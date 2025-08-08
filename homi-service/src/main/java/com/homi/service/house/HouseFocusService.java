package com.homi.service.house;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.dto.house.HouseSimpleVO;
import com.homi.domain.enums.house.OperationModeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.Focus;
import com.homi.model.entity.House;
import com.homi.model.entity.Room;
import com.homi.model.mapper.FocusMapper;
import com.homi.model.repo.FocusRepo;
import com.homi.model.repo.HouseRepo;
import com.homi.model.repo.RoomLayoutRepo;
import com.homi.model.repo.RoomRepo;
import com.homi.service.system.DeptService;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        houseRepo.save(house);

        houseCreateDto.setId(house.getId());

        Focus focus = new Focus();
        BeanUtils.copyProperties(houseCreateDto, focus);
        focus.setHouseId(house.getId());
        focus.setClosedFloors(JSONUtil.toJsonStr(houseCreateDto.getClosedFloors()));
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

    private void createFocusRoom(FocusCreateDTO houseCreateDto) {
        houseCreateDto.getRoomList().forEach(roomDTO -> {
            if (houseCreateDto.getClosedFloors().contains(roomDTO.getFloorLevel())) {
                return;
            }

            // 去掉尾号是4的房间号
            if (Boolean.TRUE.equals(houseCreateDto.getExcludeFour()) && roomDTO.getRoomNumber().endsWith("4")) {
                return;
            }

            Room room = new Room();
            room.setLocked(roomDTO.getLocked());
            room.setHouseId(houseCreateDto.getId());
            room.setCompanyId(houseCreateDto.getCompanyId());
            room.setRoomNumber(roomDTO.getRoomNumber());
            room.setFloorLevel(roomDTO.getFloorLevel());
            room.setLocked(roomDTO.getLocked());
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
