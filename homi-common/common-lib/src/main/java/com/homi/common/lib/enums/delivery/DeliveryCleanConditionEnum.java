package com.homi.common.lib.enums.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "交割单清洁情况枚举")
public enum DeliveryCleanConditionEnum {
    CLEAN("CLEAN", "整洁"),
    NORMAL("NORMAL", "一般"),
    NEED_CLEANING("NEED_CLEANING", "需清洁");

    private final String code;
    private final String name;
}
