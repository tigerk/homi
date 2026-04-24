package com.homi.service.service.room;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.room.OccupancyStatusEnum;
import com.homi.common.lib.enums.room.RoomLockReasonEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.JsonUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.house.dto.FacilityItemDTO;
import com.homi.model.house.dto.HouseLayoutDTO;
import com.homi.model.house.vo.HouseDetailVO;
import com.homi.model.room.dto.RoomIdDTO;
import com.homi.model.room.dto.RoomLockDTO;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.dto.RoomSaveRemarkDTO;
import com.homi.model.room.dto.price.PriceConfigDTO;
import com.homi.model.room.vo.*;
import com.homi.model.tenant.vo.LeaseLiteVO;
import com.homi.service.service.price.PriceConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepo roomRepo;
    private final RoomDetailRepo roomDetailRepo;
    private final FocusRepo focusRepo;
    private final RoomPriceConfigRepo roomPriceConfigRepo;
    private final RoomPricePlanRepo roomPricePlanRepo;
    private final BookingRepo bookingRepo;
    private final LeaseRepo leaseRepo;
    private final TenantRepo tenantRepo;
    private final HouseRepo houseRepo;
    private final HouseLayoutRepo houseLayoutRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final RoomLockRepo roomLockRepo;
    private final UserRepo userRepo;
    private final PriceConfigService priceConfigService;

    /**
     * 获取房间列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @param query 参数说明
     * @return com.homi.common.model.response.ResponseResult<com.homi.domain.dto.room.RoomListVO>
     */
    public PageVO<RoomListVO> getRoomList(RoomQueryDTO query) {
        Page<RoomListVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<RoomListVO> roomPage = roomRepo.getBaseMapper().pageRoomList(page, query);

        roomPage.getRecords().forEach(this::format);

        // 封装返回结果
        PageVO<RoomListVO> pageVO = new PageVO<>();
        pageVO.setTotal(roomPage.getTotal());
        pageVO.setList(roomPage.getRecords());
        pageVO.setCurrentPage(roomPage.getCurrent());
        pageVO.setPageSize(roomPage.getSize());
        pageVO.setPages(roomPage.getPages());

        return pageVO;
    }

    public void format(RoomListVO room) {
        if (room.getLeaseMode().equals(LeaseModeEnum.FOCUS.getCode())) {
            Focus byId = focusRepo.getById(room.getLeaseModeId());
            room.setCommunityName(byId.getFocusName());
        }


        OccupancyStatusEnum occupancyStatusEnum = EnumUtil.getBy(OccupancyStatusEnum::getCode, room.getOccupancyStatus());
        room.setOccupancyStatusName(occupancyStatusEnum.getName());
        room.setOccupancyStatusColor(occupancyStatusEnum.getColor());
    }

    /**
     * 获取房间状态统计
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @param query 查询参数
     * @return java.util.List<com.homi.domain.vo.room.RoomTotalItemVO>
     */
    public RoomTotalVO getRoomStatusTotal(RoomQueryDTO query) {
        // 查询时不传 occupancyStatus / locked / closed，统计全量
        // 1. 业务状态统计（GROUP BY occupancy_status，只统计 closed=0 且 locked=0 的）
        List<RoomOccupancyStatusTotalVO> statusRows = roomRepo.getBaseMapper().getStatusTotal(query);
        Map<Integer, Integer> statusCountMap = statusRows.stream()
            .collect(Collectors.toMap(RoomOccupancyStatusTotalVO::getOccupancyStatus, RoomOccupancyStatusTotalVO::getTotal));

        // 2. 管理状态统计
        int lockedCount = roomRepo.countByLocked(query);
        int closedCount = roomRepo.countByClosed(query);

        // 3. 按顺序组装 statusList
        List<RoomTotalItemVO> statusList = new ArrayList<>();
        for (OccupancyStatusEnum e : OccupancyStatusEnum.values()) {
            statusList.add(RoomDisplayStatus.buildStatusItem(e, statusCountMap.getOrDefault(e.getCode(), 0)));
        }
        statusList.add(RoomDisplayStatus.buildClosedItem(closedCount));
        statusList.add(RoomDisplayStatus.buildLockedItem(lockedCount));

        // 4. 全部 = 所有状态数量之和
        int total = statusRows.stream().mapToInt(RoomOccupancyStatusTotalVO::getTotal).sum();

        RoomTotalVO result = new RoomTotalVO();
        result.setTotal(total);
        result.setStatusList(statusList);
        return result;
    }

    /**
     * 获取房间状态枚举映射
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @return java.util.Map<java.lang.Integer, com.homi.domain.vo.room.RoomTotalItemVO>
     */
    private @NotNull Map<Integer, RoomTotalItemVO> getRoomTotalItemMap() {
        Map<Integer, RoomTotalItemVO> result = new HashMap<>();
        OccupancyStatusEnum[] values = OccupancyStatusEnum.values();
        for (OccupancyStatusEnum occupancyStatusEnum : values) {
            RoomTotalItemVO roomTotalItemVO = new RoomTotalItemVO();
            roomTotalItemVO.setRoomStatus(occupancyStatusEnum.getCode());
            roomTotalItemVO.setRoomStatusName(occupancyStatusEnum.getName());
            roomTotalItemVO.setRoomStatusColor(occupancyStatusEnum.getColor());
            roomTotalItemVO.setTotal(0);
            result.put(occupancyStatusEnum.getCode(), roomTotalItemVO);
        }
        return result;
    }

    /**
     * 计算房间出租率和数量
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/24 01:30
     *
     * @param roomsList 参数说明
     * @return cn.hutool.core.lang.Pair<java.lang.Long,java.math.BigDecimal>
     */
    private Pair<Long, BigDecimal> calculateLeasedRateAndCount(List<RoomListVO> roomsList) {
        // 计算出租率
        long leasedCount = roomsList.stream()
            .map(RoomListVO::getOccupancyStatus)
            .filter(status -> status != null && status.equals(OccupancyStatusEnum.LEASED.getCode()))
            .count();

        BigDecimal leasedRate = BigDecimal.ZERO;
        if (!roomsList.isEmpty()) {
            leasedRate = BigDecimal.valueOf(leasedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(roomsList.size()), 2, RoundingMode.HALF_UP);
        }

        return Pair.of(leasedCount, leasedRate);
    }

    /**
     * 获取房间列表（按房源ID）
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/11 13:41
     *
     * @param id 参数说明
     * @return java.util.List<com.homi.domain.dto.room.RoomCreateDTO>
     */
    public List<RoomDetailVO> getRoomDetailByHouseId(Long id) {
        List<Room> roomListByHouseId = roomRepo.getRoomListByHouseId(id);

        return roomListByHouseId.stream().map(room -> buildRoomDetailVO(room, false)).toList();
    }

    public RoomDetailVO getRoomDetail(Long roomId) {
        Room room = getRoomById(roomId);
        if (Objects.isNull(room)) {
            throw new BizException("房间不存在");
        }
        return buildRoomDetailVO(room, true);
    }

    private RoomDetailVO buildRoomDetailVO(Room room, boolean includeHouse) {
        RoomDetailVO roomDetailVO = new RoomDetailVO();
        BeanUtils.copyProperties(room, roomDetailVO);

        RoomDetail roomDetail = roomDetailRepo.getByRoomId(room.getId());
        if (Objects.nonNull(roomDetail)) {
            BeanUtils.copyProperties(roomDetail, roomDetailVO);
        }

        if (JsonUtils.isJson(room.getTags())) {
            roomDetailVO.setTags(JSONUtil.toList(room.getTags(), String.class));
        }
        if (JsonUtils.isJson(room.getVideoList())) {
            roomDetailVO.setVideoList(JSONUtil.toList(room.getVideoList(), String.class));
        }
        if (JsonUtils.isJson(room.getImageList())) {
            roomDetailVO.setImageList(JSONUtil.toList(room.getImageList(), String.class));
        }
        if (JsonUtils.isJson(room.getFacilities())) {
            roomDetailVO.setFacilities(JSONUtil.toList(room.getFacilities(), FacilityItemDTO.class));
        }

        PriceConfigDTO priceConfigByRoomId = priceConfigService.getPriceConfigByRoomId(room.getId());
        if (Objects.isNull(priceConfigByRoomId.getPrice())) {
            priceConfigByRoomId.setPrice(room.getPrice());
        }
        roomDetailVO.setPriceConfig(priceConfigByRoomId);

        if (includeHouse) {
            House house = houseRepo.getById(room.getHouseId());
            if (Objects.nonNull(house)) {
                HouseDetailVO houseDetailVO = new HouseDetailVO();
                BeanUtils.copyProperties(house, houseDetailVO);
                if (Objects.nonNull(house.getHouseLayoutId())) {
                    HouseLayoutDTO houseLayout = houseLayoutRepo.getHouseLayoutById(house.getHouseLayoutId());
                    houseDetailVO.setHouseLayout(houseLayout);
                }
                roomDetailVO.setHouse(houseDetailVO);
            }
        }

        return roomDetailVO;
    }

    public List<RoomListVO> getRoomListByRoomIds(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptyList(); // 直接返回空列表，避免后续处理。
        }

        RoomQueryDTO roomQueryDTO = new RoomQueryDTO();
        roomQueryDTO.setRoomIds(roomIds);

        IPage<RoomListVO> roomListVOIPage = roomRepo.pageRoomGridList(roomQueryDTO);
        return roomListVOIPage.getRecords();
    }

    /**
     * 获取房间租约信息（按房间ID）
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/2/23 03:06
     *
     * @param roomId     参数说明
     * @param roomStatus 参数说明
     * @return com.homi.model.room.vo.LeaseInfoVO
     */
    public LeaseInfoVO getRoomLeaseInfo(Long roomId, Integer roomStatus) {
        if (Objects.equals(roomStatus, OccupancyStatusEnum.LEASED.getCode())) {
            // 查询
            LeaseLiteVO lease = leaseRepo.getCurrentLeasesByRoomId(roomId);
            if (lease != null) {
                Tenant tenant = tenantRepo.getById(lease.getTenantId());
                return LeaseInfoVO.builder()
                    .leaseId(lease.getLeaseId())
                    .tenantName(tenant != null ? tenant.getTenantName() : "")
                    .tenantPhone(tenant != null ? tenant.getTenantPhone() : "")
                    .leaseStartDate(lease.getLeaseStart())
                    .leaseEndDate(lease.getLeaseEnd())
                    .build();
            }
        }

        if (Objects.equals(roomStatus, OccupancyStatusEnum.BOOKED.getCode())) {
            // 查询当前租客的租约信息
            Booking booking = bookingRepo.getCurrentBookingByRoomId(roomId);
            if (booking != null) {
                return LeaseInfoVO.builder()
                    .bookingId(booking.getId())
                    .tenantName(booking.getTenantName())
                    .tenantPhone(booking.getTenantPhone())
                    .leaseStartDate(booking.getExpectedLeaseStart())
                    .leaseEndDate(booking.getExpectedLeaseEnd())
                    .build();
            }
        }

        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean lockRoom(RoomLockDTO lockDTO) {
        Room room = roomRepo.getById(lockDTO.getRoomId());
        if (Objects.isNull(room)) {
            throw new BizException("房间不存在");
        }

        if (Objects.equals(lockDTO.getLockReason(), RoomLockReasonEnum.SPECIFIED_TIME.getCode())) {
            if (Objects.isNull(lockDTO.getStartAt()) || Objects.isNull(lockDTO.getEndAt())) {
                throw new BizException("指定时间锁房必须填写开始时间和结束时间");
            }
            if (lockDTO.getEndAt().before(lockDTO.getStartAt())) {
                throw new BizException("结束时间不能早于开始时间");
            }
        }

        Boolean locked = roomRepo.lockRoomById(lockDTO.getRoomId());
        if (Boolean.FALSE.equals(locked)) {
            throw new BizException("锁房失败");
        }

        // 关闭同房间的旧有效锁房记录，避免出现多条 lock_status=1 的历史记录
        roomLockRepo.lambdaUpdate()
            .eq(RoomLock::getRoomId, lockDTO.getRoomId())
            .eq(RoomLock::getLockStatus, StatusEnum.ACTIVE.getValue())
            .set(RoomLock::getLockStatus, StatusEnum.DISABLED.getValue())
            .set(RoomLock::getUpdateBy, lockDTO.getUpdateBy())
            .update();

        RoomLock roomLock = BeanCopyUtils.copyBean(lockDTO, RoomLock.class);
        assert roomLock != null;
        roomLock.setCompanyId(room.getCompanyId());
        roomLock.setLockStatus(StatusEnum.ACTIVE.getValue());
        roomLock.setCreateBy(lockDTO.getUpdateBy());
        roomLock.setUpdateBy(lockDTO.getUpdateBy());
        roomLockRepo.save(roomLock);

        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean unlockRoom(RoomIdDTO query) {
        Boolean unlocked = roomRepo.unlockRoomById(query.getRoomId());

        roomLockRepo.lambdaUpdate()
            .eq(RoomLock::getRoomId, query.getRoomId())
            .eq(RoomLock::getLockStatus, StatusEnum.ACTIVE.getValue())
            .set(RoomLock::getLockStatus, StatusEnum.DISABLED.getValue())
            .set(RoomLock::getUpdateBy, query.getUpdateBy())
            .update();

        return unlocked;
    }

    public Boolean closeRoom(RoomIdDTO query) {
        return roomRepo.closeRoomById(query.getRoomId());
    }

    public Boolean openRoom(RoomIdDTO query) {
        return roomRepo.openRoomById(query.getRoomId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer unlockExpiredTimedLocks() {
        Date now = DateUtil.date();
        List<RoomLock> expiredLocks = roomLockRepo.list(new LambdaQueryWrapper<RoomLock>()
            .eq(RoomLock::getLockReason, RoomLockReasonEnum.SPECIFIED_TIME.getCode())
            .eq(RoomLock::getLockStatus, StatusEnum.ACTIVE.getValue())
            .isNotNull(RoomLock::getEndAt)
            .le(RoomLock::getEndAt, now));

        int count = 0;
        for (RoomLock lock : expiredLocks) {
            roomRepo.unlockRoomById(lock.getRoomId());
            lock.setLockStatus(StatusEnum.DISABLED.getValue());
            lock.setUpdateBy(0L);
            roomLockRepo.updateById(lock);
            count++;
        }
        return count;
    }

    public List<RoomLockRecordVO> getRoomLockRecords(Long roomId) {
        if (Objects.isNull(roomId)) {
            throw new BizException("房间ID不能为空");
        }

        List<RoomLock> lockList = roomLockRepo.list(new LambdaQueryWrapper<RoomLock>()
            .eq(RoomLock::getRoomId, roomId)
            .orderByDesc(RoomLock::getCreateAt)
            .orderByDesc(RoomLock::getId));

        if (CollUtil.isEmpty(lockList)) {
            return List.of();
        }

        Set<Long> userIds = lockList.stream()
            .flatMap(lock -> Arrays.stream(new Long[]{lock.getCreateBy(), lock.getUpdateBy()}))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, String> userNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            userRepo.listByIds(userIds).forEach(user -> {
                String displayName = CharSequenceUtil.isNotBlank(user.getNickname()) ? user.getNickname() : user.getUsername();
                userNameMap.put(user.getId(), displayName);
            });
        }

        return lockList.stream().map(lock -> {
            RoomLockRecordVO vo = new RoomLockRecordVO();
            BeanUtils.copyProperties(lock, vo);

            RoomLockReasonEnum reasonEnum = EnumUtil.getBy(RoomLockReasonEnum::getCode, lock.getLockReason());
            vo.setLockReasonName(Objects.nonNull(reasonEnum) ? reasonEnum.getName() : "-");
            vo.setLockStatusName(Objects.equals(lock.getLockStatus(), StatusEnum.ACTIVE.getValue()) ? "生效中" : "已失效");
            vo.setCreateByName(userNameMap.getOrDefault(lock.getCreateBy(), "-"));
            vo.setUpdateByName(userNameMap.getOrDefault(lock.getUpdateBy(), "-"));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据房间ID列表获取拼接后的地址字符串
     * <p>
     * 多个房间用 "、" 连接，例如：
     * - 单间: "12312栋12单元-104室"
     * - 多间: "12312栋12单元-104室、12312栋12单元-105室"
     *
     * @param roomIds 房间ID列表
     * @return 拼接后的房间地址，如果为空返回空字符串
     */
    public String getRoomAddressByIds(List<Long> roomIds) {
        if (CollUtil.isEmpty(roomIds)) {
            return "";
        }

        List<Room> rooms = roomRepo.listByIds(roomIds);
        if (CollUtil.isEmpty(rooms)) {
            return "";
        }

        return rooms.stream().map(this::buildRoomAddress).filter(StrUtil::isNotBlank).collect(Collectors.joining("、"));
    }

    /**
     * 拼接单个房间的地址
     * 格式: {楼栋名}{单元名}-{房间号}
     * 示例: "12312栋12单元-104室"
     * <p>
     * 请根据你实际的 Room 实体字段名做调整，
     * 下面列出了几种常见的字段命名方式。
     */
    private String buildRoomAddress(Room room) {
        House house = houseRepo.getById(room.getHouseId());

        return String.format("%s-%s", house.getHouseName(), room.getRoomNumber());
    }

    public Room getRoomById(Long roomId) {
        return roomRepo.getById(roomId);
    }

    public LeaseLiteVO getCurrentLeasesByRoomId(Long roomId) {
        LeaseLiteVO currentLeasesByRoomId = leaseRepo.getCurrentLeasesByRoomId(roomId);
        if (Objects.isNull(currentLeasesByRoomId)) {
            return null;
        }

        List<LeaseRoom> listByLeaseId = leaseRoomRepo.getListByLeaseId(currentLeasesByRoomId.getLeaseId());
        List<Long> roomIds = listByLeaseId.stream().map(LeaseRoom::getRoomId).collect(Collectors.toList());
        currentLeasesByRoomId.setRoomIds(roomIds);

        List<RoomListVO> roomList = getRoomListByRoomIds(roomIds);
        currentLeasesByRoomId.setRoomList(roomList);

        return currentLeasesByRoomId;
    }

    public Long addRoomRemark(RoomSaveRemarkDTO dto) {
        Room room = roomRepo.getById(dto.getRoomId());
        room.setRemark(dto.getRemark());
        room.setUpdateBy(dto.getUpdateBy());
        roomRepo.updateById(room);

        return room.getId();
    }
}
