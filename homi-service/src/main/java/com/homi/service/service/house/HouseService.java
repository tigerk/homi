package com.homi.service.service.house;

import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.model.booking.vo.BookingListVO;
import com.homi.model.community.dto.CommunityDTO;
import com.homi.model.company.vo.user.UserLiteVO;
import com.homi.model.dao.entity.Booking;
import com.homi.model.dao.entity.Dept;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.entity.OwnerContractHouse;
import com.homi.model.dao.entity.Room;
import com.homi.model.dao.repo.*;
import com.homi.model.house.dto.HouseQueryDTO;
import com.homi.model.house.dto.HouseLayoutDTO;
import com.homi.model.house.vo.HouseDetailVO;
import com.homi.model.house.vo.HouseListVO;
import com.homi.model.room.vo.RoomDetailVO;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/23
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepo houseRepo;
    private final CommunityRepo communityRepo;
    private final HouseLayoutRepo houseLayoutRepo;
    private final UserRepo userRepo;
    private final BookingRepo bookingRepo;
    private final DeptRepo deptRepo;
    private final RoomTrackRepo roomTrackRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractHouseRepo ownerContractHouseRepo;
    private final RoomRepo roomRepo;

    private final RoomService roomService;

    /**
     * 根据房源ID获取房源详情。
     *
     * @param houseId 房源的唯一标识符
     * @return 包含房源详细信息的HouseDetailVO对象，包括小区、户型和房间列表等信息
     */
    public HouseDetailVO getHouseDetailById(Long houseId) {
        House house = houseRepo.getById(houseId);

        HouseDetailVO houseDetail = new HouseDetailVO();
        BeanUtils.copyProperties(house, houseDetail);

        // 加载销售代表数据
        UserLiteVO salesman = userRepo.getUserLiteById(house.getSalesmanId());
        houseDetail.setSalesman(salesman);

        // 加载小区数据
        CommunityDTO communityDTO = communityRepo.getCommunityById(house.getCommunityId());
        houseDetail.setCommunity(communityDTO);

        Dept dept = deptRepo.getById(house.getDeptId());
        if (Objects.nonNull(dept)) {
            houseDetail.setDeptName(dept.getName());
        }

        // 加载户型数据
        HouseLayoutDTO houseLayoutById = houseLayoutRepo.getHouseLayoutById(house.getHouseLayoutId());
        houseDetail.setHouseLayout(houseLayoutById);

        List<RoomDetailVO> roomList = roomService.getRoomDetailByHouseId(house.getId());
        roomList.forEach(room -> {
            room.setLease(roomService.getCurrentLeasesByRoomId(room.getId()));
            Booking currentBookingByRoomId = bookingRepo.getCurrentBookingByRoomId(room.getId());
            if (Objects.nonNull(currentBookingByRoomId)) {
                BookingListVO bookingListVO = BeanCopyUtils.copyBean(currentBookingByRoomId, BookingListVO.class);
                room.setBooking(bookingListVO);
            }

            room.setRoomTracks(roomTrackRepo.getRoomTracksByRoomId(room.getId()));
        });

        houseDetail.setRoomList(roomList);

        return houseDetail;
    }

    public PageVO<HouseListVO> getAvailableHouseList(HouseQueryDTO query) {
        Set<Long> occupiedHouseIds = getOccupiedHouseIds(query.getCompanyId(), query.getExcludeOwnerContractId());

        Page<House> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        IPage<House> housePage = houseRepo.lambdaQuery()
            .eq(House::getCompanyId, query.getCompanyId())
            .and(cn.hutool.core.text.CharSequenceUtil.isNotBlank(query.getKeywords()), wrapper -> wrapper
                .like(House::getHouseName, query.getKeywords())
                .or()
                .like(House::getHouseCode, query.getKeywords())
                .or()
                .like(House::getBuilding, query.getKeywords())
                .or()
                .like(House::getUnit, query.getKeywords())
                .or()
                .like(House::getDoorNumber, query.getKeywords()))
            .notIn(!CollectionUtils.isEmpty(occupiedHouseIds), House::getId, occupiedHouseIds)
            .orderByDesc(House::getUpdateTime)
            .page(page);

        List<HouseListVO> list = housePage.getRecords().stream()
            .map(this::buildHouseListVO)
            .toList();

        return PageVO.<HouseListVO>builder()
            .currentPage(housePage.getCurrent())
            .pageSize(housePage.getSize())
            .total(housePage.getTotal())
            .pages(housePage.getPages())
            .list(list)
            .build();
    }

    private Set<Long> getOccupiedHouseIds(Long companyId, Long excludeOwnerContractId) {
        List<Long> contractIds = ownerContractRepo.lambdaQuery()
            .eq(OwnerContract::getCompanyId, companyId)
            .ne(Objects.nonNull(excludeOwnerContractId), OwnerContract::getId, excludeOwnerContractId)
            .list()
            .stream()
            .map(OwnerContract::getId)
            .toList();

        if (CollectionUtils.isEmpty(contractIds)) {
            return Collections.emptySet();
        }

        return ownerContractHouseRepo.lambdaQuery()
            .in(OwnerContractHouse::getContractId, contractIds)
            .list()
            .stream()
            .map(OwnerContractHouse::getHouseId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private HouseListVO buildHouseListVO(House house) {
        HouseListVO vo = new HouseListVO();
        vo.setHouseId(house.getId());
        vo.setHouseName(house.getHouseName());
        vo.setHouseCode(house.getHouseCode());
        vo.setBuilding(house.getBuilding());
        vo.setUnit(house.getUnit());
        vo.setDoorNumber(house.getDoorNumber());
        vo.setRentalType(house.getRentalType());
        vo.setArea(house.getArea());
        vo.setRoomCount(house.getRoomCount());
        vo.setCertificateNo(house.getCertificateNo());
        vo.setAddressText(buildAddressText(house));
        vo.setLayoutText(buildLayoutText(house.getHouseLayoutId()));
        vo.setCommunityName(getCommunityName(house.getCommunityId()));
        vo.setReferenceRentAmount(getReferenceRentAmount(house.getId()));
        return vo;
    }

    private String getCommunityName(Long communityId) {
        if (Objects.isNull(communityId)) {
            return null;
        }
        CommunityDTO community = communityRepo.getCommunityById(communityId);
        return Objects.nonNull(community) ? community.getName() : null;
    }

    private String buildAddressText(House house) {
        List<String> parts = new ArrayList<>();
        if (Objects.nonNull(house.getCommunityId())) {
            CommunityDTO community = communityRepo.getCommunityById(house.getCommunityId());
            if (Objects.nonNull(community) && cn.hutool.core.text.CharSequenceUtil.isNotBlank(community.getName())) {
                parts.add(community.getName());
            }
        }
        if (cn.hutool.core.text.CharSequenceUtil.isNotBlank(house.getBuilding())) {
            parts.add(house.getBuilding());
        }
        if (cn.hutool.core.text.CharSequenceUtil.isNotBlank(house.getUnit())) {
            parts.add(house.getUnit());
        }
        if (cn.hutool.core.text.CharSequenceUtil.isNotBlank(house.getDoorNumber())) {
            parts.add(house.getDoorNumber());
        }
        return String.join(" ", parts);
    }

    private String buildLayoutText(Long houseLayoutId) {
        if (Objects.isNull(houseLayoutId)) {
            return "-";
        }
        HouseLayoutDTO layout = houseLayoutRepo.getHouseLayoutById(houseLayoutId);
        if (Objects.isNull(layout)) {
            return "-";
        }
        return String.format("%s室%s厅%s卫",
            Objects.requireNonNullElse(layout.getBedroom(), 0),
            Objects.requireNonNullElse(layout.getLivingRoom(), 0),
            Objects.requireNonNullElse(layout.getBathroom(), 0));
    }

    private BigDecimal getReferenceRentAmount(Long houseId) {
        List<Room> rooms = roomRepo.getRoomListByHouseId(houseId);
        if (CollectionUtils.isEmpty(rooms)) {
            return null;
        }
        return rooms.stream()
            .map(Room::getPrice)
            .filter(Objects::nonNull)
            .min(BigDecimal::compareTo)
            .orElse(null);
    }
}
