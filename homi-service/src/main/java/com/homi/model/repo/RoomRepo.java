package com.homi.model.repo;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.focus.FocusHouseDTO;
import com.homi.domain.dto.room.RoomAggregatedDTO;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.model.entity.Room;
import com.homi.model.mapper.RoomMapper;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<FocusHouseDTO> getRoomListByHouseId(Long houseId) {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getHouseId, houseId);

        return getBaseMapper().selectList(queryWrapper).stream()
                .map(room -> BeanCopyUtils.copyBean(room, FocusHouseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 计算房间状态
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:53
     *
     * @param room 参数说明
     * @return com.homi.domain.enums.RoomStatusEnum
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

    public List<RoomAggregatedDTO> selectAggregatedRooms(RoomQueryDTO query) {
        return getBaseMapper().selectAggregatedRooms(query);
    }

    public IPage<RoomItemDTO> pageRoomGridList(List<RoomAggregatedDTO> currentQueryStatistic, RoomQueryDTO query) {
        List<Long> communityIds = currentQueryStatistic.stream().map(RoomAggregatedDTO::getCommunityId).distinct().toList();
        List<String> buildings = currentQueryStatistic.stream().map(RoomAggregatedDTO::getBuilding).distinct().toList();
        List<String> units = currentQueryStatistic.stream().map(RoomAggregatedDTO::getUnit).distinct().toList();
        List<Integer> floors = currentQueryStatistic.stream().map(RoomAggregatedDTO::getFloor).distinct().toList();

        query.setCommunityIds(communityIds);
        query.setBuildings(buildings);
        query.setUnits(units);
        query.setFloors(floors);

        Page<RoomItemDTO> page = new Page<>(1, Integer.MAX_VALUE);
        return getBaseMapper().pageRoomList(page, query);
    }

}
