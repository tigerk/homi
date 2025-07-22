package com.homi.service.house;

import com.homi.domain.dto.house.FocusCreateDTO;
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
        houseCreateDto.setDeptId(userDept.getId());
        houseCreateDto.setCompanyId(userDept.getCompanyId());

        House house = new House();
        BeanUtils.copyProperties(houseCreateDto, house);
        houseRepo.save(house);

        houseCreateDto.setId(house.getId());

        HouseFocus houseFocus = new HouseFocus();
        BeanUtils.copyProperties(houseCreateDto, houseFocus);
        houseFocus.setHouseId(house.getId());
        houseFocusRepo.save(houseFocus);

        // 创建房间
        createFocusRoom(houseCreateDto);

        return true;
    }

    /**
     * 根据 FocusCreateDTO 的 roomLayouts 创建房间数据，并插入到 Room 表中
     *
     * @param houseCreateDto 集中式房源创建 DTO
     */
    private void createFocusRoom(FocusCreateDTO houseCreateDto) {
        // 遍历每个房型布局
        houseCreateDto.getRoomLayouts().forEach(roomLayoutDTO -> {
            RoomLayout roomLayout = new RoomLayout();
            BeanUtils.copyProperties(roomLayoutDTO, roomLayout);
            roomLayout.setHouseId(houseCreateDto.getId());
            roomLayout.setCompanyId(houseCreateDto.getCompanyId());
            roomLayoutRepo.save(roomLayout);
            // 遍历每个房间号
            roomLayoutDTO.getRoomList().forEach(roomDTO -> {
                // 创建房间实体
                Room room = new Room();
                room.setCompanyId(houseCreateDto.getCompanyId());
                room.setHouseId(houseCreateDto.getId());
                room.setRoomLayoutId(roomLayout.getId());
                room.setRoomNumber(roomDTO.getRoomNumber());
                room.setInsideSpace(roomLayoutDTO.getInsideSpace());
                room.setLeasePrice(roomLayoutDTO.getLeasePrice());
                // 保存房间到数据库
                roomRepo.save(room);
            });
        });
    }

    public Boolean updateHouseFocus(FocusCreateDTO houseCreateDto) {
        return true;
    }
}
