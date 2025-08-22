package com.homi.model.repo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.house.FocusRoomDTO;
import com.homi.model.entity.House;
import com.homi.model.entity.HouseLayout;
import com.homi.model.entity.Room;
import com.homi.model.mapper.RoomMapper;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
@RequiredArgsConstructor
public class RoomRepo extends ServiceImpl<RoomMapper, Room> {
    /**
     * 根据房源id和房间号查询房间，此数据只有一条数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 14:17
     *
     * @param houseId    房源id
     * @param roomNumber 房间号
     * @return com.homi.model.entity.Room
     */
    public Room getRoomByHouseIdAndRoomNumber(Long houseId, String roomNumber) {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getHouseId, houseId);
        queryWrapper.eq(Room::getRoomNumber, roomNumber);
        return getOne(queryWrapper);
    }

    public List<FocusRoomDTO> getRoomListByHouseId(Long houseId) {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getHouseId, houseId);

        return getBaseMapper().selectList(queryWrapper).stream().map(room -> {
            FocusRoomDTO roomDTO = BeanCopyUtils.copyBean(room, FocusRoomDTO.class);

            return roomDTO;
        }).collect(Collectors.toList());
    }
}
