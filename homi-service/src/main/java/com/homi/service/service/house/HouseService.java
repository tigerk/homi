package com.homi.service.service.house;

import com.homi.model.community.dto.CommunityDTO;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.*;
import com.homi.model.house.dto.HouseLayoutDTO;
import com.homi.model.house.vo.HouseDetailVO;
import com.homi.model.room.vo.RoomDetailVO;
import com.homi.service.service.booking.BookingService;
import com.homi.service.service.room.RoomService;
import com.homi.service.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    private final RoomService roomService;
    private final UserRepo userRepo;

    private final LeaseRepo leaseRepo;
    private final TenantService tenantService;
    private final BookingService bookingService;

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

        User userById = userRepo.getById(house.getSalesmanId());
        if (Objects.nonNull(userById)) {
            houseDetail.setSalesmanName(userById.getNickname());
        }

        // 加载小区数据
        CommunityDTO communityDTO = communityRepo.getCommunityById(house.getCommunityId());
        houseDetail.setCommunity(communityDTO);

        // 加载户型数据
        HouseLayoutDTO houseLayoutById = houseLayoutRepo.getHouseLayoutById(house.getHouseLayoutId());
        houseDetail.setHouseLayout(houseLayoutById);

        List<RoomDetailVO> roomList = roomService.getRoomDetailByHouseId(house.getId());
        roomList.forEach(room -> {
            room.setLease(tenantService.getCurrentLeaseByRoomId(room.getId()));
        });


        houseDetail.setRoomList(roomList);

        return houseDetail;
    }
}
