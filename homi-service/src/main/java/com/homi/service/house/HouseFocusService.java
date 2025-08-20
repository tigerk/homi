package com.homi.service.house;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.dto.house.HouseSimpleVO;
import com.homi.domain.enums.house.OperationModeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.Focus;
import com.homi.model.entity.House;
import com.homi.model.entity.HouseLayout;
import com.homi.model.entity.Room;
import com.homi.model.mapper.FocusMapper;
import com.homi.model.repo.*;
import com.homi.service.system.DeptService;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/7/21
 */

@Service
@Slf4j
public class HouseFocusService {
    @Resource
    private HouseRepo houseRepo;

    @Resource
    private FocusRepo focusRepo;

    @Resource
    private FocusMapper focusMapper;

    @Resource
    private UserService userService;

    @Resource
    private DeptService deptService;

    @Resource
    private RoomLayoutRepo roomLayoutRepo;

    @Resource
    private RoomRepo roomRepo;

    @Resource
    private HouseLayoutRepo houseLayoutRepo;

    /**
     * 创建集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/21 22:05
     *
     * @param houseCreateDto 参数说明
     * @return java.lang.Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createHouseFocus(FocusCreateDTO houseCreateDto) {
        if (houseRepo.checkHouseCodeExist(houseCreateDto.getHouseCode())) {
            throw new BizException("项目编号已存在");
        }

        House house = new House();
        BeanUtils.copyProperties(houseCreateDto, house);
        // 运营模式
        house.setOperationMode(OperationModeEnum.FOCUS.getCode());
        // 设置标签
        house.setTags(JSONUtil.toJsonStr(houseCreateDto.getTags()));
        houseRepo.save(house);

        houseCreateDto.setId(house.getId());

        Focus focus = new Focus();
        BeanUtils.copyProperties(houseCreateDto, focus);
        focus.setHouseId(house.getId());
        focus.setClosedFloors(JSONUtil.toJsonStr(houseCreateDto.getClosedFloors()));
        focus.setProjectFileList(JSONUtil.toJsonStr(houseCreateDto.getProjectFileList()));
        focusRepo.getBaseMapper().insert(focus);

        houseCreateDto.setId(focus.getId());
        // 创建房间
        createFocusRoom(houseCreateDto);

        boolean b = houseRepo.updateHouseRoomCount(house.getId());
        if (!b) {
            log.warn("更新房源房间数量：houseId={}, resp={}", house.getId(), b);
        }

        return house.getId();
    }

    /**
     * 创建集中式的房间、更新户型
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/20 13:50
     *
     * @param houseCreateDto 参数说明
     */
    private void createFocusRoom(FocusCreateDTO houseCreateDto) {
        Map<Long, Long> houseLayoutIdMap = new HashMap<>();
        houseCreateDto.getHouseLayoutList().forEach(houseLayoutDTO -> {
            HouseLayout houseLayout = new HouseLayout();
            BeanUtils.copyProperties(houseLayoutDTO, houseLayout, "id");
            houseLayout.setHouseId(houseCreateDto.getId());
            houseLayout.setCompanyId(houseCreateDto.getCompanyId());
            houseLayout.setCreateBy(houseCreateDto.getCreateBy());
            houseLayout.setCreateTime(houseCreateDto.getCreateTime());
            houseLayout.setUpdateBy(houseCreateDto.getUpdateBy());
            houseLayout.setUpdateTime(houseCreateDto.getUpdateTime());

            houseLayoutRepo.getBaseMapper().insert(houseLayout);
            houseLayoutIdMap.put(houseLayoutDTO.getId(), houseLayout.getId());
        });

        houseCreateDto.getRoomList().forEach(roomDTO -> {
            if (CollUtil.isNotEmpty(houseCreateDto.getClosedFloors()) && houseCreateDto.getClosedFloors().contains(roomDTO.getFloor())) {
                return;
            }

            Room room = new Room();
            room.setLocked(roomDTO.getLocked());
            room.setHouseId(houseCreateDto.getId());
            room.setCompanyId(houseCreateDto.getCompanyId());
            room.setRoomNumber(roomDTO.getRoomNumber());
            room.setFloor(roomDTO.getFloor());
            room.setLocked(roomDTO.getLocked());
            room.setHouseLayoutId(houseLayoutIdMap.get(roomDTO.getHouseLayoutId()));
            room.setUpdateBy(houseCreateDto.getUpdateBy());
            room.setUpdateTime(houseCreateDto.getUpdateTime());

            if (Objects.nonNull(roomDTO.getId())) {
                Room roomBefore = roomRepo.getRoomByHouseIdAndRoomNumber(houseCreateDto.getId(), roomDTO.getRoomNumber());
                BeanUtils.copyProperties(room, roomBefore);
                roomRepo.getBaseMapper().updateById(roomBefore);
            } else {
                room.setCreateBy(houseCreateDto.getCreateBy());
                room.setCreateTime(houseCreateDto.getCreateTime());

                room.setVacancyStartTime(DateUtil.date());
                roomRepo.getBaseMapper().insert(room);
            }

            roomDTO.setId(room.getId());
        });
    }


    public Long updateHouseFocus(FocusCreateDTO houseCreateDto) {
        Optional<House> optById = houseRepo.getOptById(houseCreateDto.getId());
        if (optById.isEmpty()) {
            throw new BizException("房源不存在");
        }

        House house = optById.get();
        BeanUtils.copyProperties(houseCreateDto, house);
        houseRepo.updateById(house);

        houseCreateDto.setId(house.getId());

        Focus focus = new Focus();
        BeanUtils.copyProperties(houseCreateDto, focus);
        focus.setHouseId(house.getId());
        focusRepo.updateById(focus);

        // 更新房间
        createFocusRoom(houseCreateDto);

        return house.getId();
    }

    public List<HouseSimpleVO> getHouseOptionList(OperationModeEnum operationModeEnum) {
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(House::getOperationMode, operationModeEnum.getCode());

        List<House> list = houseRepo.list(queryWrapper);

        return list.stream().map(house -> BeanCopyUtils.copyBean(house, HouseSimpleVO.class)).toList();
    }
}
