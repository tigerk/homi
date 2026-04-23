package com.homi.common.lib.enums.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "交割单项目编码枚举")
public enum DeliveryItemCodeEnum {
    WATER_METER("WATER_METER", "水表读数"),
    ELECTRICITY_METER("ELECTRICITY_METER", "电表读数"),
    GAS_METER("GAS_METER", "燃气表读数");

    private final String code;
    private final String name;
}
