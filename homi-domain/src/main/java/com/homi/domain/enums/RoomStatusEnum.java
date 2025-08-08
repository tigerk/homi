package com.homi.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomStatusEnum {
    /**
     * 房间状态枚举
     */
    AVAILABLE(0, "空置"),
    LEASED(1, "已租"),
    LOCKED(2, "锁房"),
    PREPARING(3, "配置中"),
    OBSOLETE(4, "下架"),

    ;

    private final Integer code;

    private final String name;
}
