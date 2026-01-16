package com.homi.common.lib.enums.room;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomStatusEnum {
    /**
     * 0-空置：可展示、可带看、可预定、可签约
     * 1-已租：已有生效合同，不可预定，不可签约
     * 2-已预定：已有预定单且在有效期内，不可再次预定，不可直接签约（需走转合同流程）
     * 3-配置中：新房装修或清扫中，通常不可签约
     * 4-已关闭：房间下架或禁用
     * 5-锁房：通常指非业务类的锁定（如业主自住、行政查封）
     */
    AVAILABLE(0, "空置", "#FF2800"),

    /**
     * 1-已租：已有生效合同，不可预定，不可签约
     */
    LEASED(1, "已租", "#52C41A"),

    /**
     * 2-已预定：已有预定单且在有效期内，不可再次预定，不可直接签约（需走转合同流程）
     */
    BOOKED(2, "已预定", "#EAA212"),

    /**
     * 3-配置中：新房装修或清扫中，通常不可签约
     */
    PREPARING(3, "配置中", "#4B50AD"),

    /**
     * 4-已关闭：房间下架或禁用
     */
    CLOSED(4, "已关闭", "#DBDBDB"),

    /**
     * 5-锁房：通常指非业务类的锁定（如业主自住、行政查封）
     */
    LOCKED(5, "锁房", "#8C8C8C"),
    ;

    private final Integer code;

    private final String name;

    private final String color;


}
