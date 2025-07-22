package com.homi.service.house;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.dto.house.FocusRoomLayoutDTO;
import com.homi.exception.BizException;
import com.homi.model.entity.*;
import com.homi.model.mapper.HouseFocusMapper;
import com.homi.model.repo.HouseFocusRepo;
import com.homi.model.repo.HouseRepo;
import com.homi.model.repo.RoomLayoutRepo;
import com.homi.model.repo.RoomRepo;
import com.homi.service.system.DeptService;
import com.homi.service.system.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
public class HouseFocusService {
    @Resource
    private HouseRepo houseRepo;

    @Resource
    private HouseFocusRepo houseFocusRepo;

    @Resource
    private HouseFocusMapper houseFocusMapper;

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
    public Boolean createHouseFocus(FocusCreateDTO houseCreateDto) {
        Dept userDept = deptService.getUserDept(houseCreateDto.getSalesmanId());
        if (userDept != null) {
            houseCreateDto.setDeptId(userDept.getId());
            houseCreateDto.setCompanyId(userDept.getCompanyId());
        }

        House house = new House();
        BeanUtils.copyProperties(houseCreateDto, house);
        houseRepo.save(house);

        houseCreateDto.setId(house.getId());

        HouseFocus houseFocus = new HouseFocus();
        BeanUtils.copyProperties(houseCreateDto, houseFocus);
        houseFocus.setHouseId(house.getId());
        houseFocus.setClosedFloors(JSONUtil.toJsonStr(houseCreateDto.getClosedFloors()));
        houseFocusRepo.getBaseMapper().insert(houseFocus);

        // 创建房间 & 房型
        createFocusRoomAndLayout(houseCreateDto);

        return true;
    }

    /**
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/22 18:01
     *
     * @param houseCreateDto 参数说明
     */
    private void createFocusRoomAndLayout(FocusCreateDTO houseCreateDto) {
        Map<String, FocusRoomLayoutDTO> roomLayout2number = new HashMap<>();
        // 遍历每个房型布局
        houseCreateDto.getRoomLayouts().forEach(roomLayoutDTO -> {
            RoomLayout roomLayout = new RoomLayout();
            BeanUtils.copyProperties(roomLayoutDTO, roomLayout);
            roomLayout.setHouseId(houseCreateDto.getId());
            roomLayout.setCompanyId(houseCreateDto.getCompanyId());
            roomLayout.setInsideSpace(roomLayoutDTO.getInsideSpace());
            roomLayoutRepo.saveOrUpdate(roomLayout);

            roomLayoutDTO.setId(roomLayout.getId());

            roomLayoutDTO.getRoomNumbers().forEach(roomNumberDTO -> {
                roomLayout2number.put(roomNumberDTO, roomLayoutDTO);
            });
        });

        houseCreateDto.getRooms().forEach(roomDTO -> {
            Room room = new Room();
            room.setLocked(roomDTO.getLocked());
            room.setHouseId(houseCreateDto.getId());
            room.setCompanyId(houseCreateDto.getCompanyId());
            room.setRoomNumber(roomDTO.getRoomNumber());
            room.setFloorLevel(roomDTO.getFloorLevel());

            if (roomLayout2number.containsKey(roomDTO.getRoomNumber())) {
                FocusRoomLayoutDTO focusRoomLayoutDTO = roomLayout2number.get(roomDTO.getRoomNumber());
                room.setRoomLayoutId(focusRoomLayoutDTO.getId());
                room.setLeasePrice(focusRoomLayoutDTO.getLeasePrice());
                room.setInsideSpace(focusRoomLayoutDTO.getInsideSpace());
            }

            if (Objects.nonNull(roomDTO.getId())) {
                Room roomBefore = roomRepo.getById(roomDTO.getId());
                BeanUtils.copyProperties(room, roomBefore);
                roomRepo.getBaseMapper().updateById(roomBefore);
            } else {
                room.setVacancyStartTime(DateUtil.date());
                roomRepo.getBaseMapper().insert(room);
            }

            roomDTO.setId(room.getId());
        });
    }

    public Boolean updateHouseFocus(FocusCreateDTO houseCreateDto) {
        Dept userDept = deptService.getUserDept(houseCreateDto.getSalesmanId());
        if (userDept != null) {
            houseCreateDto.setDeptId(userDept.getId());
            houseCreateDto.setCompanyId(userDept.getCompanyId());
        }

        Optional<House> optById = houseRepo.getOptById(houseCreateDto.getId());
        if (optById.isEmpty()) {
            throw new BizException("房源不存在");
        }

        House house = optById.get();
        BeanUtils.copyProperties(houseCreateDto, house);
        houseRepo.updateById(house);

        houseCreateDto.setId(house.getId());

        HouseFocus houseFocus = new HouseFocus();
        BeanUtils.copyProperties(houseCreateDto, houseFocus);
        houseFocus.setHouseId(house.getId());
        houseFocusRepo.updateById(houseFocus);

        // 创建房间
        createFocusRoomAndLayout(houseCreateDto);

        return true;
    }
}
