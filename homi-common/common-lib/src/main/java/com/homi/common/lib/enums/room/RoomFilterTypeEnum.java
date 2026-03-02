package com.homi.common.lib.enums.room;

import lombok.AllArgsConstructor;
import lombok.Getter;

// com.homi.common.lib.enums.room.RoomFilterTypeEnum.java
@Getter
@AllArgsConstructor
public enum RoomFilterTypeEnum {
    BY_STATUS(0, "按业务状态筛选"),
    BY_LOCKED(1, "按锁定状态筛选"),
    BY_CLOSED(2, "按关闭状态筛选");

    private final Integer code;
    private final String desc;
}
