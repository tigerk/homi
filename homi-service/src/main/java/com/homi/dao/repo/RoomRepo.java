package com.homi.dao.repo;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.enums.room.RoomStatusEnum;
import com.homi.domain.vo.room.RoomListVO;
import com.homi.domain.vo.room.grid.RoomAggregatedVO;
import com.homi.dao.entity.Room;
import com.homi.dao.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Room> getRoomListByHouseId(Long houseId) {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getHouseId, houseId);


        return getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 计算房间状态
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:53
     *
     * @param room 参数说明
     * @return com.homi.domain.enums.room.RoomStatusEnum
     */
    public RoomStatusEnum calculateRoomStatus(Room room) {
        if (Boolean.TRUE.equals(room.getLeased())) {
            return RoomStatusEnum.LEASED;
        }

        if (Boolean.TRUE.equals(room.getClosed())) {
            return RoomStatusEnum.CLOSED;
        }

        if (Boolean.TRUE.equals(room.getLocked())) {
            return RoomStatusEnum.LOCKED;
        }
        if (room.getVacancyStartTime() != null && room.getVacancyStartTime().after(DateUtil.date())) {
            return RoomStatusEnum.PREPARING;
        }

        return RoomStatusEnum.AVAILABLE;
    }

    public List<RoomAggregatedVO> selectAggregatedRooms(RoomQueryDTO query) {
        return getBaseMapper().selectAggregatedRooms(query);
    }

    public IPage<RoomListVO> pageRoomGridList(RoomQueryDTO query) {
        Page<RoomListVO> page = new Page<>(1, Integer.MAX_VALUE);
        return getBaseMapper().pageRoomList(page, query);
    }

}
