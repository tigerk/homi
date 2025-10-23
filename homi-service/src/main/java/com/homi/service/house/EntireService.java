package com.homi.service.house;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.homi.domain.dto.house.HouseLayoutDTO;
import com.homi.domain.dto.scatter.EntireCreateDTO;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.domain.enums.house.LeaseModeEnum;
import com.homi.model.entity.Community;
import com.homi.model.entity.House;
import com.homi.model.entity.HouseLayout;
import com.homi.model.entity.Room;
import com.homi.model.repo.*;
import com.homi.service.room.RoomSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/23
 */

@Service
@Slf4j
public class EntireService {
    @Resource
    private HouseRepo houseRepo;

    @Resource
    private CommunityRepo communityRepo;

    @Resource
    private RoomRepo roomRepo;

    @Resource
    private HouseLayoutRepo houseLayoutRepo;

    @Resource
    private RoomSearchService roomSearchService;

    @Resource
    private UploadedFileRepo uploadedFileRepo;

    /**
     * 创建整租房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/23 10:19
     *
     * @param entireCreateDTO 参数说明
     * @return java.lang.Long
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createHouseEntire(EntireCreateDTO entireCreateDTO) {
        Community community = communityRepo.createCommunity(entireCreateDTO.getCommunity());
        entireCreateDTO.getCommunity().setCommunityId(community.getId());

        // 创建整租房源
        createHouses(entireCreateDTO);

        // 设置上传文件为已使用
        Optional<List<String>> imageList = entireCreateDTO.getHouseList().stream()
                .map(h -> h.getHouseLayout().getImageList())
                .reduce((v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                });
        imageList.ifPresent(strings -> uploadedFileRepo.setFileUsedByName(strings));

        return Boolean.TRUE;
    }

    private void createHouses(EntireCreateDTO entireCreateDTO) {
        entireCreateDTO.getHouseList().forEach(houseDTO -> {
            String address = String.format("%s%s%s栋%s-%s室", entireCreateDTO.getCommunity().getDistrict(),
                    entireCreateDTO.getCommunity().getName(),
                    houseDTO.getBuilding(),
                    CharSequenceUtil.isBlank(houseDTO.getUnit()) ? "" : houseDTO.getUnit() + "单元",
                    houseDTO.getDoorNumber());

            House house = new House();

            Boolean exist = houseRepo.checkHouseExist(entireCreateDTO.getCommunity().getCommunityId(), houseDTO.getBuilding(), houseDTO.getUnit(), houseDTO.getDoorNumber());
            if (Boolean.TRUE.equals(exist)) {
                throw new IllegalArgumentException(address + " 已存在！");
            }

            // 创建户型数据
            Long layoutId = createScatterHouseLayout(entireCreateDTO, houseDTO.getHouseLayout());
            house.setHouseLayoutId(layoutId);

            house.setLeaseMode(LeaseModeEnum.SCATTER.getCode());
            house.setModeRefId(entireCreateDTO.getCommunity().getCommunityId());

            house.setCommunityId(entireCreateDTO.getCommunity().getCommunityId());
            house.setHouseCode(houseDTO.getHouseCode());
            house.setBuilding(houseDTO.getBuilding());
            house.setUnit(houseDTO.getUnit());
            house.setDoorNumber(houseDTO.getDoorNumber());
            house.setFloor(houseDTO.getFloor());
            house.setFloorTotal(houseDTO.getFloorTotal());
            house.setRentalType(houseDTO.getRentalType());
            house.setArea(houseDTO.getArea());
            house.setDirection(houseDTO.getDirection());
            house.setWater(entireCreateDTO.getWater());
            house.setElectricity(entireCreateDTO.getElectricity());
            house.setHeating(entireCreateDTO.getHeating());
            house.setHasElevator(entireCreateDTO.getHasElevator());

            house.setDeptId(entireCreateDTO.getDeptId());
            house.setSalesmanId(entireCreateDTO.getSalesmanId());

            house.setHouseName(address);

            house.setCreateBy(entireCreateDTO.getCreateBy());
            house.setCreateTime(entireCreateDTO.getCreateTime());
            house.setUpdateBy(entireCreateDTO.getCreateBy());
            house.setUpdateTime(entireCreateDTO.getCreateTime());

            houseRepo.saveHouse(house);

            createEntireRoom(house, houseDTO.getPrice());
        });
    }

    public Long createScatterHouseLayout(EntireCreateDTO entireCreateDTO, HouseLayoutDTO houseLayoutDTO) {
        HouseLayout houseLayout = new HouseLayout();
        BeanUtils.copyProperties(houseLayoutDTO, houseLayout, "id");
        houseLayout.setLeaseMode(LeaseModeEnum.SCATTER.getCode());
        houseLayout.setModeRefId(entireCreateDTO.getCommunity().getCommunityId());
        houseLayout.setCompanyId(entireCreateDTO.getCompanyId());

        houseLayout.setFacilities(JSONUtil.toJsonStr(houseLayoutDTO.getFacilities()));
        // 设置标签
        houseLayout.setTags(JSONUtil.toJsonStr(houseLayoutDTO.getTags()));
        houseLayout.setImageList(JSONUtil.toJsonStr(houseLayoutDTO.getImageList()));
        houseLayout.setVideoList(JSONUtil.toJsonStr(houseLayoutDTO.getVideoList()));

        if (houseLayoutDTO.getNewly().equals(Boolean.TRUE)) {
            houseLayoutRepo.getBaseMapper().insert(houseLayout);
        } else {
            houseLayout.setId(houseLayoutDTO.getId());
            houseLayoutRepo.getBaseMapper().updateById(houseLayout);
        }

        return houseLayout.getId();
    }

    /**
     * 设置价格
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/23 15:40
     *
     * @param house 参数说明
     * @param price 参数说明
     */
    private void createEntireRoom(House house, BigDecimal price) {
        Room room = new Room();

        BeanUtils.copyProperties(house, room);

        room.setHouseId(house.getId());
        room.setPrice(price);

        RoomStatusEnum roomStatusEnum = roomRepo.calculateRoomStatus(room);
        room.setRoomStatus(roomStatusEnum.getCode());
        room.setKeywords(roomSearchService.generateKeywords(room));
        room.setRoomNumber(house.getDoorNumber());

        Room roomBefore = roomRepo.getRoomByHouseIdAndRoomNumber(house.getId(), house.getDoorNumber());
        if (Objects.nonNull(roomBefore)) {
            room.setId(roomBefore.getId());
            roomRepo.getBaseMapper().updateById(room);
        } else {
            room.setCreateBy(house.getCreateBy());
            room.setCreateTime(house.getCreateTime());

            room.setVacancyStartTime(DateUtil.date());
            roomRepo.getBaseMapper().insert(room);
        }
    }

    public Boolean updateHouseEntire(EntireCreateDTO entireCreateDTO) {
        return Boolean.TRUE;
    }
}
