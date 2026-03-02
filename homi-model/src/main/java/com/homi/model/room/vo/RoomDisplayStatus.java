package com.homi.model.room.vo;

import com.homi.common.lib.enums.room.OccupancyStatusEnum;
import com.homi.common.lib.enums.room.RoomFilterTypeEnum;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/3/2
 */
public record RoomDisplayStatus(String name, String color) {
    public static RoomDisplayStatus of(Integer roomStatus, Boolean locked, Boolean closed) {
        if (Boolean.TRUE.equals(closed)) {
            return new RoomDisplayStatus("已关闭", "#DBDBDB");
        }
        if (Boolean.TRUE.equals(locked)) {
            return new RoomDisplayStatus("锁房", "#8C8C8C");
        }
        OccupancyStatusEnum e = OccupancyStatusEnum.of(roomStatus);
        return new RoomDisplayStatus(e.getName(), e.getColor());
    }

    /**
     * 构建业务状态统计项
     */
    public static RoomTotalItemVO buildStatusItem(OccupancyStatusEnum e, Integer total) {
        RoomTotalItemVO vo = new RoomTotalItemVO();
        vo.setRoomStatus(e.getCode());
        vo.setRoomStatusName(e.getName());
        vo.setRoomStatusColor(e.getColor());
        vo.setFilterType(RoomFilterTypeEnum.BY_STATUS.getCode());
        vo.setTotal(total);
        return vo;
    }

    /**
     * 构建锁房统计项
     */
    public static RoomTotalItemVO buildLockedItem(int lockedCount) {
        RoomTotalItemVO vo = new RoomTotalItemVO();
        vo.setRoomStatus(null);          // 没有对应的 occupancyStatus
        vo.setRoomStatusName("锁房");
        vo.setRoomStatusColor("#8C8C8C");
        vo.setFilterType(RoomFilterTypeEnum.BY_LOCKED.getCode());
        vo.setTotal(lockedCount);
        return vo;
    }

    /**
     * 构建已关闭统计项
     */
    public static RoomTotalItemVO buildClosedItem(Integer total) {
        RoomTotalItemVO vo = new RoomTotalItemVO();
        vo.setRoomStatus(null);
        vo.setRoomStatusName("已关闭");
        vo.setRoomStatusColor("#DBDBDB");
        vo.setFilterType(RoomFilterTypeEnum.BY_CLOSED.getCode());
        vo.setTotal(total);
        return vo;
    }
}
