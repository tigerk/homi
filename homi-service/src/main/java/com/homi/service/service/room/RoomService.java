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
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.owner.OwnerContractStatusEnum;
import com.homi.common.lib.enums.room.OccupancyStatusEnum;
import com.homi.common.lib.enums.room.RoomLockReasonEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.JsonUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.booking.vo.BookingListVO;
import com.homi.model.community.dto.CommunityDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.house.dto.FacilityItemDTO;
import com.homi.model.house.dto.HouseLayoutDTO;
import com.homi.model.house.vo.HouseDetailVO;
import com.homi.model.room.dto.RoomIdDTO;
import com.homi.model.room.dto.RoomLockDTO;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.dto.RoomSaveRemarkDTO;
import com.homi.model.room.dto.RoomDeleteDTO;
import com.homi.model.room.dto.RoomRestoreDTO;
import com.homi.model.room.dto.price.PriceConfigDTO;
import com.homi.model.room.vo.*;
import com.homi.model.tenant.vo.LeaseLiteVO;
import com.homi.service.bizlog.BizOperateLogService;
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
    private final CommunityRepo communityRepo;
    private final DeptRepo deptRepo;
    private final RoomTrackRepo roomTrackRepo;
    private final PriceConfigService priceConfigService;
    private final BizOperateLogService bizOperateLogService;

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

    public List<CommunityDTO> getRoomCommunityOptions(RoomQueryDTO query) {
        return roomRepo.getBaseMapper().selectRoomCommunityOptions(query);
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
        Room room = roomRepo.getByIdIncludeDeleted(roomId);
        if (Objects.isNull(room)) {
            throw new BizException("房间不存在");
        }
        RoomDetailVO roomDetail = buildRoomDetailVO(room, true);
        enrichRoomBusinessInfo(roomDetail);
        return roomDetail;
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
            House house = houseRepo.getByIdIncludeDeleted(room.getHouseId());
            if (Objects.nonNull(house)) {
                HouseDetailVO houseDetailVO = new HouseDetailVO();
                BeanUtils.copyProperties(house, houseDetailVO);
                houseDetailVO.setSalesman(userRepo.getUserLiteById(house.getSalesmanId()));
                houseDetailVO.setCommunity(communityRepo.getCommunityById(house.getCommunityId()));
                Dept dept = deptRepo.getById(house.getDeptId());
                if (Objects.nonNull(dept)) {
                    houseDetailVO.setDeptName(dept.getName());
                }
                if (Objects.nonNull(house.getHouseLayoutId())) {
                    HouseLayoutDTO houseLayout = houseLayoutRepo.getHouseLayoutById(house.getHouseLayoutId());
                    houseDetailVO.setHouseLayout(houseLayout);
                }
                List<RoomDetailVO> roomList = getRoomDetailByHouseId(house.getId());
                roomList.forEach(this::enrichRoomBusinessInfo);
                houseDetailVO.setRoomList(roomList);
                roomDetailVO.setHouse(houseDetailVO);
            }
        }

        return roomDetailVO;
    }

    /**
     * 补充房间详情页展示所需的当前租约、预定单和跟进记录。
     * room/detail 是房间详情页唯一数据入口，因此这里要与 house/detail 的展示数据口径保持一致。
     */
    private void enrichRoomBusinessInfo(RoomDetailVO room) {
        if (Objects.isNull(room) || Objects.isNull(room.getId())) {
            return;
        }

        room.setLease(getDisplayLeaseByRoomId(room.getId()));

        Booking currentBooking = bookingRepo.getCurrentBookingByRoomId(room.getId());
        if (Objects.nonNull(currentBooking)) {
            room.setBooking(BeanCopyUtils.copyBean(currentBooking, BookingListVO.class));
        }

        room.setRoomTracks(roomTrackRepo.getRoomTracksByRoomId(room.getId()));
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
            LeaseLiteVO lease = leaseRepo.getDisplayLeaseByRoomId(roomId);
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
    public Boolean deleteRoom(RoomDeleteDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getRoomId())) {
            throw new BizException("房间ID不能为空");
        }
        if (CharSequenceUtil.isBlank(dto.getDeleteReason())) {
            throw new BizException("删除原因不能为空");
        }

        Room room = roomRepo.getByIdIncludeDeleted(dto.getRoomId());
        if (Objects.isNull(room)) {
            throw new BizException("房间不存在");
        }
        if (Boolean.TRUE.equals(room.getDeleted())) {
            throw new BizException("房间已删除");
        }

        House house = houseRepo.getByIdIncludeDeleted(room.getHouseId());
        if (Objects.isNull(house)) {
            throw new BizException("房源不存在");
        }

        validateRoomCanDelete(room, house);

        Date now = DateUtil.date();
        Long operatorId = dto.getUpdateBy();
        Map<String, Object> beforeSnapshot = buildRoomDeleteSnapshot(room, house);
        boolean deleted = roomRepo.markDeleted(room.getId(), dto.getDeleteReason().trim(), operatorId, now);
        if (!deleted) {
            throw new BizException("删除房间失败，请刷新后重试");
        }
        Room deletedRoom = roomRepo.getByIdIncludeDeleted(room.getId());
        saveBizOperateLog(BizOperateBizTypeEnum.ROOM, room.getId(), BizOperateTypeEnum.DELETE, "删除房间",
            dto.getDeleteReason().trim(), beforeSnapshot, buildRoomDeleteSnapshot(deletedRoom, house),
            room.getCompanyId(), operatorId);

        if (roomRepo.countActiveByHouseId(room.getHouseId()) == 0 && !Boolean.TRUE.equals(house.getDeleted())) {
            Map<String, Object> beforeHouseSnapshot = buildHouseDeleteSnapshot(house);
            boolean houseDeleted = houseRepo.markDeleted(house.getId(), dto.getDeleteReason().trim(), operatorId, now);
            if (houseDeleted) {
                House deletedHouse = houseRepo.getByIdIncludeDeleted(house.getId());
                saveBizOperateLog(BizOperateBizTypeEnum.HOUSE, house.getId(), BizOperateTypeEnum.DELETE, "自动删除房源",
                    "删除最后一个房间后同步删除房源：" + dto.getDeleteReason().trim(),
                    beforeHouseSnapshot, buildHouseDeleteSnapshot(deletedHouse), house.getCompanyId(), operatorId);
            }
        }

        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean restoreRoom(RoomRestoreDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getRoomId())) {
            throw new BizException("房间ID不能为空");
        }

        Room room = roomRepo.getByIdIncludeDeleted(dto.getRoomId());
        if (Objects.isNull(room)) {
            throw new BizException("房间不存在");
        }
        if (!Boolean.TRUE.equals(room.getDeleted())) {
            throw new BizException("房间未删除，无需恢复");
        }

        House house = houseRepo.getByIdIncludeDeleted(room.getHouseId());
        if (Objects.isNull(house)) {
            throw new BizException("房源不存在");
        }
        if (roomRepo.existsActiveSameRoomNumber(room.getHouseId(), room.getRoomNumber(), room.getId())) {
            throw new BizException("同一房源下已存在相同房号的未删除房间，无法恢复");
        }

        Date now = DateUtil.date();
        Long operatorId = dto.getUpdateBy();
        String restoreReason = CharSequenceUtil.blankToDefault(dto.getRestoreReason(), "恢复误删房间").trim();

        if (Boolean.TRUE.equals(house.getDeleted())) {
            Map<String, Object> beforeHouseSnapshot = buildHouseDeleteSnapshot(house);
            boolean houseRestored = houseRepo.markRestored(house.getId(), restoreReason, operatorId, now);
            if (houseRestored) {
                House restoredHouse = houseRepo.getByIdIncludeDeleted(house.getId());
                saveBizOperateLog(BizOperateBizTypeEnum.HOUSE, house.getId(), BizOperateTypeEnum.RESTORE, "自动恢复房源",
                    "恢复房间时同步恢复房源：" + restoreReason,
                    beforeHouseSnapshot, buildHouseDeleteSnapshot(restoredHouse), house.getCompanyId(), operatorId);
                house = restoredHouse;
            }
        }

        Map<String, Object> beforeSnapshot = buildRoomDeleteSnapshot(room, house);
        boolean restored = roomRepo.markRestored(room.getId(), restoreReason, operatorId, now);
        if (!restored) {
            throw new BizException("恢复房间失败，请刷新后重试");
        }
        Room restoredRoom = roomRepo.getByIdIncludeDeleted(room.getId());
        saveBizOperateLog(BizOperateBizTypeEnum.ROOM, room.getId(), BizOperateTypeEnum.RESTORE, "恢复房间",
            restoreReason, beforeSnapshot, buildRoomDeleteSnapshot(restoredRoom, house), room.getCompanyId(), operatorId);

        return Boolean.TRUE;
    }

    private void validateRoomCanDelete(Room room, House house) {
        List<Integer> ownerActiveStatuses = List.of(
            OwnerContractStatusEnum.PENDING_APPROVAL.getCode(),
            OwnerContractStatusEnum.PENDING_SIGN.getCode(),
            OwnerContractStatusEnum.SIGNED.getCode()
        );
        if (roomRepo.existsActiveOwnerContract(room, house, ownerActiveStatuses)) {
            throw new BizException("该房间已关联业主合同，请先处理业主合同后再删除");
        }
        if (CollUtil.isNotEmpty(leaseRepo.listOccupyingLeasesByRoomIds(List.of(room.getId())))) {
            throw new BizException("该房间已有租客租约，请先退租或作废租约后再删除");
        }
        if (bookingRepo.existsActiveByRoomId(room.getId())) {
            throw new BizException("该房间存在预约中记录，请先取消预约后再删除");
        }
    }

    private Map<String, Object> buildRoomDeleteSnapshot(Room room, House house) {
        if (Objects.isNull(room)) {
            return null;
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("房间ID", room.getId());
        snapshot.put("房源ID", room.getHouseId());
        snapshot.put("房源名称", Objects.nonNull(house) ? house.getHouseName() : null);
        snapshot.put("房号", room.getRoomNumber());
        snapshot.put("删除状态", Boolean.TRUE.equals(room.getDeleted()) ? "已删除" : "正常");
        snapshot.put("删除原因", room.getDeleteReason());
        snapshot.put("删除人", room.getDeleteBy());
        snapshot.put("删除时间", room.getDeleteAt());
        snapshot.put("恢复原因", room.getRestoreReason());
        snapshot.put("恢复人", room.getRestoreBy());
        snapshot.put("恢复时间", room.getRestoreAt());
        return snapshot;
    }

    private Map<String, Object> buildHouseDeleteSnapshot(House house) {
        if (Objects.isNull(house)) {
            return null;
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("房源ID", house.getId());
        snapshot.put("房源名称", house.getHouseName());
        snapshot.put("房源编号", house.getHouseCode());
        snapshot.put("删除状态", Boolean.TRUE.equals(house.getDeleted()) ? "已删除" : "正常");
        snapshot.put("删除原因", house.getDeleteReason());
        snapshot.put("删除人", house.getDeleteBy());
        snapshot.put("删除时间", house.getDeleteAt());
        snapshot.put("恢复原因", house.getRestoreReason());
        snapshot.put("恢复人", house.getRestoreBy());
        snapshot.put("恢复时间", house.getRestoreAt());
        return snapshot;
    }

    private void saveBizOperateLog(BizOperateBizTypeEnum bizType, Long bizId, BizOperateTypeEnum operateType, String operateDesc,
                                   String remark, Object beforeSnapshot, Object afterSnapshot, Long companyId, Long operatorId) {
        User operator = Objects.nonNull(operatorId) ? userRepo.getById(operatorId) : null;
        String operatorName = "-";
        if (Objects.nonNull(operator)) {
            operatorName = CharSequenceUtil.blankToDefault(operator.getRealName(), CharSequenceUtil.blankToDefault(operator.getNickname(), operator.getUsername()));
        }

        BizOperateSourceTypeEnum sourceType = BizOperateBizTypeEnum.HOUSE.equals(bizType) ? BizOperateSourceTypeEnum.HOUSE : BizOperateSourceTypeEnum.ROOM;
        bizOperateLogService.saveLog(
            companyId,
            bizType.getCode(),
            bizId,
            operateType.getCode(),
            operateDesc,
            remark,
            beforeSnapshot,
            afterSnapshot,
            Map.of("reason", remark),
            sourceType.getCode(),
            bizId,
            operatorId,
            operatorName
        );
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

    public LeaseLiteVO getDisplayLeaseByRoomId(Long roomId) {
        LeaseLiteVO displayLease = leaseRepo.getDisplayLeaseByRoomId(roomId);
        if (Objects.isNull(displayLease)) {
            return null;
        }

        List<LeaseRoom> listByLeaseId = leaseRoomRepo.getListByLeaseId(displayLease.getLeaseId());
        List<Long> roomIds = listByLeaseId.stream().map(LeaseRoom::getRoomId).collect(Collectors.toList());
        displayLease.setRoomIds(roomIds);

        List<RoomListVO> roomList = getRoomListByRoomIds(roomIds);
        displayLease.setRoomList(roomList);

        return displayLease;
    }

    public Long addRoomRemark(RoomSaveRemarkDTO dto) {
        Room room = roomRepo.getById(dto.getRoomId());
        room.setRemark(dto.getRemark());
        room.setUpdateBy(dto.getUpdateBy());
        roomRepo.updateById(room);

        return room.getId();
    }
}
