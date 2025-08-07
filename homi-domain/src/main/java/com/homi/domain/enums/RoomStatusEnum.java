package com.homi.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomStatusEnum {
    /**
     * 房间状态枚举
     */
    AVAILABLE(1, "可租"),
    LEASED(2, "已租"),
    LOCKED(3, "锁房"),
    PREPARING(4, "配置中"),
    OBSOLETE(5, "下架"),

    ;

    private final Integer code;

    private final String name;
}
