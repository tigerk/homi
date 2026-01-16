package com.homi.model.dao.repo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.Room;
import com.homi.model.dao.mapper.RoomMapper;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.model.room.vo.grid.RoomAggregatedVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        if (room.getRoomStatus().equals(RoomStatusEnum.LEASED.getCode())) {
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

    public Boolean updateRoomStatusBatch(List<Long> roomIds, Integer code) {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Room::getId, roomIds);

        Room room = new Room();
        room.setRoomStatus(code);

        return update(room, queryWrapper);
    }

    /**
     * 批量更新房间状态，一部分房间变为 AVAILABLE，一部分房间变为 BOOKED
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/9 11:48
     *
     * @param toRelease 要释放的房间id列表
     * @param toBook    要预定的房间id列表
     * @return java.lang.Boolean
     */
    public Boolean batchUpdateRoomStatusMixed(List<Long> toRelease, List<Long> toBook) {
        // 1. 释放房间：从预定状态变回空置
        if (CollUtil.isNotEmpty(toRelease)) {
            boolean releaseResult = lambdaUpdate()
                .in(Room::getId, toRelease)
                .set(Room::getRoomStatus, RoomStatusEnum.AVAILABLE.getCode())
                // 关键点：重置空置开始时间为当前时间
                .set(Room::getVacancyStartTime, DateUtil.date())
                .update();

            if (!releaseResult) {
                throw new BizException("释放房间状态失败");
            }
        }

        // 2. 预定房间：从空置状态变为预定
        if (CollUtil.isNotEmpty(toBook)) {
            // 使用 LambdaUpdateWrapper 确保原子性和条件检查
            boolean bookResult = lambdaUpdate()
                .in(Room::getId, toBook)
                // 只有处于“空置”状态的房间才能被预定，防止并发冲突覆盖已租或已锁房间
                .eq(Room::getRoomStatus, RoomStatusEnum.AVAILABLE.getCode())
                .set(Room::getRoomStatus, RoomStatusEnum.BOOKED.getCode())
                // 预定后，空置开始时间可以清空，也可以保持，取决于你是否需要在预定时也计算空置时长
                // .set(Room::getVacancyStartTime, null)
                .update();

            if (!bookResult) {
                // 如果受影响行数不匹配，MyBatis-Plus 的 update 会返回 false
                // 这里抛出异常会触发事务回滚，保证数据一致性
                throw new BizException("部分房间已被占用或状态已变更，请刷新后重试");
            }
        }

        return true;
    }
}
