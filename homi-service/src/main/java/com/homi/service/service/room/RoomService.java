package com.homi.service.service.room;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.utils.JsonUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.house.dto.FacilityItemDTO;
import com.homi.model.room.dto.RoomDetailDTO;
import com.homi.model.room.dto.RoomIdDTO;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.dto.price.OtherFeeDTO;
import com.homi.model.room.dto.price.PriceConfigDTO;
import com.homi.model.room.dto.price.PricePlanDTO;
import com.homi.model.room.vo.LeaseInfoVO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.model.room.vo.RoomTotalItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
    private final TenantRepo tenantRepo;

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
     * @return java.util.List<com.homi.domain.dto.room.RoomDetailDTO>
     */
    public List<RoomDetailDTO> getRoomListByHouseId(Long id) {
        List<Room> roomListByHouseId = roomRepo.getRoomListByHouseId(id);

        return roomListByHouseId.stream().map(room -> {
            RoomDetailDTO roomDetailDTO = new RoomDetailDTO();
            BeanUtils.copyProperties(room, roomDetailDTO);

            RoomDetail roomDetail = roomDetailRepo.getByRoomId(room.getId());
            if (Objects.nonNull(roomDetail)) {
                BeanUtils.copyProperties(roomDetail, roomDetailDTO);
            }

            if (JsonUtils.isJson(room.getTags())) {
                roomDetailDTO.setTags(JSONUtil.toList(room.getTags(), String.class));
            }
            if (JsonUtils.isJson(room.getVideoList())) {
                roomDetailDTO.setVideoList(JSONUtil.toList(room.getVideoList(), String.class));
            }
            if (JsonUtils.isJson(room.getImageList())) {
                roomDetailDTO.setImageList(JSONUtil.toList(room.getImageList(), String.class));
            }
            if (JsonUtils.isJson(room.getFacilities())) {
                roomDetailDTO.setFacilities(JSONUtil.toList(room.getFacilities(), FacilityItemDTO.class));
            }

            roomDetailDTO.setPriceConfig(getPriceConfigByRoomId(room.getId()));

            return roomDetailDTO;
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
     * {@code @date} 2025/11/11 13:41
     *
     * @param room 房间信息
     */
    public void getRoomLeaseInfo(RoomListVO room) {
        if (Objects.equals(room.getRoomStatus(), RoomStatusEnum.LEASED.getCode())) {
            // 查询当前租客的租约信息
            Tenant tenant = tenantRepo.getCurrentTenantByRoomId(room.getRoomId());
            if (tenant != null) {
                LeaseInfoVO build = LeaseInfoVO.builder()
                    .tenantName(tenant.getTenantName())
                    .tenantPhone(tenant.getTenantPhone())
                    .leaseStartDate(tenant.getLeaseStart())
                    .leaseEndDate(tenant.getLeaseEnd())
                    .build();

                room.setLeaseInfo(build);
            }
        }

        if (Objects.equals(room.getRoomStatus(), RoomStatusEnum.BOOKED.getCode())) {
            // 查询当前租客的租约信息
            Booking booking = bookingRepo.getCurrentBookingByRoomId(room.getRoomId());
            if (booking != null) {
                room.setLeaseInfo(LeaseInfoVO.builder()
                    .tenantName(booking.getTenantName())
                    .tenantPhone(booking.getTenantPhone())
                    .leaseStartDate(booking.getExpectedLeaseStart())
                    .leaseEndDate(booking.getExpectedLeaseEnd())
                    .build());
            }
        }
    }

    public Boolean lockRoom(RoomIdDTO query) {
        return roomRepo.lockRoomById(query.getRoomId());
    }

    public Boolean unlockRoom(RoomIdDTO query) {
        return roomRepo.unlockRoomById(query.getRoomId());
    }
}
