package com.homi.service.house;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.homi.domain.dto.house.FocusBuildingDTO;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.domain.enums.house.LeaseModeEnum;
import com.homi.domain.vo.IdNameVO;
import com.homi.exception.BizException;
import com.homi.model.entity.*;
import com.homi.model.repo.*;
import com.homi.service.room.RoomSearchService;
import com.homi.utils.BeanCopyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/7/21
 */

@Service
@Slf4j
public class FocusService {
    @Resource
    private HouseRepo houseRepo;

    @Resource
    private FocusRepo focusRepo;

    @Resource
    private FocusBuildingRepo focusBuildingRepo;

    @Resource
    private RoomRepo roomRepo;

    @Resource
    private HouseLayoutRepo houseLayoutRepo;

    @Resource
    private RoomSearchService roomSearchService;

    @Resource
    private CommunityRepo communityRepo;

    /**
     * 创建集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/21 22:05
     *
     * @param focusCreateDto 参数说明
     * @return java.lang.Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createHouseFocus(FocusCreateDTO focusCreateDto) {
        if (focusRepo.checkFocusCodeExist(focusCreateDto.getFocusCode())) {
            throw new BizException("项目编号（" + focusCreateDto.getFocusCode() + "）已存在");
        }

        Community community = communityRepo.createCommunity(focusCreateDto.getCommunity());
        focusCreateDto.getCommunity().setCommunityId(community.getId());

        // 创建集中式项目
        Focus focus = focusRepo.saveFocus(focusCreateDto);
        focusCreateDto.setId(focus.getId());

        // 创建集中式楼栋信息
        saveFocusBuildings(focus.getId(), focusCreateDto.getBuildings());

        // 创建集中式户型
        Map<Long, Long> houseLayoutIdMap = houseLayoutRepo.createHouseLayouts(focusCreateDto);

        // 创建集中式房源
        createFocusHouses(houseLayoutIdMap, focusCreateDto);

        return focus.getId();
    }

    /**
     * 更新集中式的楼栋信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:25
     *
     * @param focusId      参数说明
     * @param buildingList 参数说明
     * @return boolean
     */
    public boolean saveFocusBuildings(Long focusId, List<FocusBuildingDTO> buildingList) {
        List<FocusBuilding> focusBuildings = buildingList.stream().map(building -> {
                FocusBuilding focusBuilding = focusBuildingRepo.getFocusBuilding(focusId, building.getBuilding(), building.getUnit());
                if (Objects.isNull(focusBuilding)) {
                    focusBuilding = BeanCopyUtils.copyBean(building, FocusBuilding.class);
                    Objects.requireNonNull(focusBuilding).setFocusId(focusId);
                } else {
                    BeanUtils.copyProperties(building, focusBuilding);
                }

                return focusBuilding;
            }
        ).toList();

        return focusBuildingRepo.saveOrUpdateBatch(focusBuildings);

    }

    /**
     * 创建集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:18
     *
     * @param houseLayoutIdMap 集中式房型
     * @param focusCreateDto   集中式房源数据
     */
    private void createFocusHouses(Map<Long, Long> houseLayoutIdMap, FocusCreateDTO focusCreateDto) {
        focusCreateDto.getHouseList().forEach(houseDTO -> {
            House house = new House();

            BeanUtils.copyProperties(focusCreateDto, house);
            BeanUtils.copyProperties(houseDTO, house);

            house.setCommunityId(focusCreateDto.getCommunity().getCommunityId());

            // 集中式标记
            house.setModeRefId(focusCreateDto.getId());
            house.setLeaseMode(LeaseModeEnum.FOCUS.getCode());

            house.setHouseLayoutId(houseLayoutIdMap.get(houseDTO.getHouseLayoutId()));

            // 创建集中式房源编号
            String houseCode = String.format("%s%s%s%s", focusCreateDto.getFocusCode(), houseDTO.getBuilding(), houseDTO.getUnit(), houseDTO.getDoorNumber());
            house.setHouseCode(houseCode);

            house.setHouseName(String.format("%s%s%s栋%s-%s室", focusCreateDto.getCommunity().getDistrict(),
                focusCreateDto.getCommunity().getName(),
                houseDTO.getBuilding(),
                CharSequenceUtil.isBlank(houseDTO.getUnit()) ? "" : "" + houseDTO.getUnit() + "单元",
                houseDTO.getDoorNumber()));

            // 冗余信息
            house.setFacilities(JSONUtil.toJsonStr(focusCreateDto.getFacilities()));
            // 设置标签
            house.setTags(JSONUtil.toJsonStr(focusCreateDto.getTags()));
            house.setImageList(JSONUtil.toJsonStr(focusCreateDto.getImageList()));
            houseRepo.saveHouse(house);

            createFocusRoom(house);
        });
    }

    /**
     * 根据房源创建房间
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:50
     *
     * @param house 参数说明
     */
    private void createFocusRoom(House house) {
        Room room = new Room();

        BeanUtils.copyProperties(house, room);

        room.setHouseId(house.getId());
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

    /**
     * 更新集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:07
     *
     * @param focusCreateDto 参数说明
     * @return java.lang.Long
     */
    public Long updateHouseFocus(FocusCreateDTO focusCreateDto) {
        Optional<Focus> optById = focusRepo.getOptById(focusCreateDto.getId());
        if (optById.isEmpty()) {
            throw new BizException("集中式项目不存在");
        }

        // 创建集中式项目
        Focus focus = focusRepo.saveFocus(focusCreateDto);

        // 创建集中式楼栋信息
        saveFocusBuildings(focus.getId(), focusCreateDto.getBuildings());

        // 创建集中式户型
        Map<Long, Long> houseLayoutIdMap = houseLayoutRepo.createHouseLayouts(focusCreateDto);

        // 创建集中式房源
        createFocusHouses(houseLayoutIdMap, focusCreateDto);

        return focus.getId();
    }

    public List<IdNameVO> getFocusOptionList() {
        List<Focus> focusList = focusRepo.list();

        return focusList.stream().map(focus -> IdNameVO.builder()
            .id(focus.getId())
            .name(String.format("%s（%s）", focus.getFocusName(), focus.getFocusCode()))
            .build()).toList();
    }

    public Boolean checkFocusCodeExist(String focusCode) {
        return focusRepo.checkFocusCodeExist(focusCode);
    }
}
