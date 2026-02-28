package com.homi.service.service.room;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.JsonUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.house.dto.FacilityItemDTO;
import com.homi.model.room.dto.RoomIdDTO;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.dto.RoomSaveRemarkDTO;
import com.homi.model.room.dto.price.OtherFeeDTO;
import com.homi.model.room.dto.price.PriceConfigDTO;
import com.homi.model.room.dto.price.PricePlanDTO;
import com.homi.model.room.vo.LeaseInfoVO;
import com.homi.model.room.vo.RoomDetailVO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.model.room.vo.RoomTotalItemVO;
import com.homi.model.tenant.vo.LeaseLiteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
    private final LeaseRoomRepo leaseRoomRepo;

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


        RoomStatusEnum roomStatusEnum = EnumUtil.getBy(RoomStatusEnum::getCode, room.getRoomStatus());
        room.setRoomStatusName(roomStatusEnum.getName());
        room.setRoomStatusColor(roomStatusEnum.getColor());
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
    public List<RoomTotalItemVO> getRoomStatusTotal(RoomQueryDTO query) {
        Map<Integer, RoomTotalItemVO> result = getRoomTotalItemMap();

        query.setRoomStatus(null);

        List<RoomTotalItemVO> statusTotal = roomRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(roomTotalItemVO -> {
            RoomTotalItemVO orDefault = result.getOrDefault(roomTotalItemVO.getRoomStatus(), roomTotalItemVO);
            orDefault.setTotal(roomTotalItemVO.getTotal());
        });

        return result.values().stream().toList();
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
        RoomStatusEnum[] values = RoomStatusEnum.values();
        for (RoomStatusEnum roomStatusEnum : values) {
            RoomTotalItemVO roomTotalItemVO = new RoomTotalItemVO();
            roomTotalItemVO.setRoomStatus(roomStatusEnum.getCode());
            roomTotalItemVO.setRoomStatusName(roomStatusEnum.getName());
            roomTotalItemVO.setRoomStatusColor(roomStatusEnum.getColor());
            roomTotalItemVO.setTotal(0);
            result.put(roomStatusEnum.getCode(), roomTotalItemVO);
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
            .map(RoomListVO::getRoomStatus)
            .filter(status -> status != null && status.equals(RoomStatusEnum.LEASED.getCode()))
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

        return roomListByHouseId.stream().map(room -> {
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

            PriceConfigDTO priceConfigByRoomId = getPriceConfigByRoomId(room.getId());
            if (Objects.isNull(priceConfigByRoomId.getPrice())) {
                priceConfigByRoomId.setPrice(room.getPrice());
            }
            roomDetailVO.setPriceConfig(priceConfigByRoomId);

            return roomDetailVO;
        }).toList();
    }

    /**
     * 获取房间价格配置（按房间ID）
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/11 13:41
     *
     * @param roomId 房间ID
     * @return com.homi.domain.dto.room.price.PriceConfigDTO
     */
    public PriceConfigDTO getPriceConfigByRoomId(Long roomId) {
        PriceConfigDTO priceConfigDTO = new PriceConfigDTO();
        priceConfigDTO.setRoomId(roomId);

        RoomPriceConfig roomPriceConfig = roomPriceConfigRepo.getByRoomId(roomId);

        if (Objects.nonNull(roomPriceConfig)) {
            BeanUtils.copyProperties(roomPriceConfig, priceConfigDTO);
            List<OtherFeeDTO> otherFeeDTOList = JSONUtil.toList(roomPriceConfig.getOtherFees(), OtherFeeDTO.class);
            priceConfigDTO.setOtherFees(otherFeeDTOList);
        }

        List<RoomPricePlan> roomPricePlanList = roomPricePlanRepo.listByRoomId(roomId);
        if (!roomPricePlanList.isEmpty()) {
            priceConfigDTO.setPricePlans(roomPricePlanList.stream().map(roomPricePlan -> {
                PricePlanDTO pricePlanDTO = new PricePlanDTO();
                BeanUtils.copyProperties(roomPricePlan, pricePlanDTO);
                return pricePlanDTO;
            }).toList());
        }

        return priceConfigDTO;
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
        if (Objects.equals(roomStatus, RoomStatusEnum.LEASED.getCode())) {
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

        if (Objects.equals(roomStatus, RoomStatusEnum.BOOKED.getCode())) {
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

    public Integer lockRoom(RoomIdDTO query) {
        Boolean locked = roomRepo.lockRoomById(query.getRoomId());
        if (Boolean.FALSE.equals(locked)) {
            throw new BizException("房间未能锁定");
        }
        return RoomStatusEnum.LOCKED.getCode();
    }

    public Integer unlockRoom(RoomIdDTO query) {
        Boolean unlocked = roomRepo.unlockRoomById(query.getRoomId());
        if (Boolean.FALSE.equals(unlocked)) {
            throw new BizException("房间未能解锁");
        }

        return RoomStatusEnum.AVAILABLE.getCode();
    }

    public Integer closeRoom(RoomIdDTO query) {
        Boolean closed = roomRepo.closeRoomById(query.getRoomId());
        if (Boolean.FALSE.equals(closed)) {
            throw new BizException("房间未能关闭");
        }

        return RoomStatusEnum.CLOSED.getCode();
    }

    public Integer openRoom(RoomIdDTO query) {
        Boolean opened = roomRepo.openRoomById(query.getRoomId());
        if (Boolean.FALSE.equals(opened)) {
            throw new BizException("房间未能开启");
        }

        return RoomStatusEnum.AVAILABLE.getCode();
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
