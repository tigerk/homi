package com.homi.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomStatusEnum {
    /**
     * 房间状态枚举
     */
    AVAILABLE(0, "空置", "#FF2800"),
    LEASED(1, "已租", "#52C41A"),
    LOCKED(2, "锁房", "#F9541C"),
    PREPARING(3, "配置中", "#4B50AD"),
    OBSOLETE(4, "下架", "#DBDBDB"),
    ;

    private final Integer code;

    private final String name;

    private final String color;


}
