package com.homi.common.lib.enums.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "交割类型枚举")
public enum DeliveryHandoverTypeEnum {
    CHECK_IN("CHECK_IN", "入住交割"),
    CHECK_OUT("CHECK_OUT", "退租交割");

    private final String code;
    private final String name;
}
