package com.homi.service.house;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.domain.enums.house.LeaseModeEnum;
import com.homi.domain.vo.IdNameVO;
import com.homi.exception.BizException;
import com.homi.model.entity.Focus;
import com.homi.model.entity.House;
import com.homi.model.entity.Room;
import com.homi.model.repo.*;
import com.homi.service.room.RoomSearchService;
import com.homi.service.room.RoomService;
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
            throw new BizException("项目编号已存在");
        }

        // 创建集中式项目
        Focus focus = focusRepo.saveFocus(focusCreateDto);

        // 创建集中式楼栋信息
        focusBuildingRepo.saveFocusBuildings(focus.getId(), focusCreateDto.getBuildingList());

        // 创建集中式户型
        Map<Long, Long> houseLayoutIdMap = houseLayoutRepo.createHouseLayouts(focusCreateDto);

        // 创建集中式房源
        createFocusHouses(focus.getId(), houseLayoutIdMap, focusCreateDto);

        return focus.getId();
    }

    /**
     * 创建集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:18
     *
     * @param focusId          集中式项目Id
     * @param houseLayoutIdMap 集中式房型
     * @param focusCreateDto   集中式房源数据
     */
    private void createFocusHouses(Long focusId, Map<Long, Long> houseLayoutIdMap, FocusCreateDTO focusCreateDto) {

        focusCreateDto.getHouseList().forEach(houseDTO -> {
            House house = new House();

            BeanUtils.copyProperties(focusCreateDto, house);
            BeanUtils.copyProperties(houseDTO, house);

            // 集中式标记
            house.setModeRefId(focusId);
            house.setLeaseMode(LeaseModeEnum.FOCUS.getCode());

            // 创建集中式房源编号
            String houseCode = String.format("%s%s%s%s", focusCreateDto.getFocusCode(), houseDTO.getBuilding(), houseDTO.getUnit(), houseDTO.getDoorNumber());
            house.setHouseCode(houseCode);

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

        RoomStatusEnum roomStatusEnum = roomRepo.calculateRoomStatus(room);
        room.setRoomStatus(roomStatusEnum.getCode());
        room.setKeywords(roomSearchService.generateKeywords(room));

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
        focusBuildingRepo.saveFocusBuildings(focus.getId(), focusCreateDto.getBuildingList());

        // 创建集中式户型
        Map<Long, Long> houseLayoutIdMap = houseLayoutRepo.createHouseLayouts(focusCreateDto);

        // 创建集中式房源
        createFocusHouses(focus.getId(), houseLayoutIdMap, focusCreateDto);

        return focus.getId();
    }

    public List<IdNameVO> getFocusOptionList(LeaseModeEnum leaseModeEnum) {
        List<Focus> focusList = focusRepo.list();

        return focusList.stream().map(focus -> IdNameVO.builder()
            .id(focus.getId())
            .name(focus.getFocusName())
            .build()).toList();
    }
}
