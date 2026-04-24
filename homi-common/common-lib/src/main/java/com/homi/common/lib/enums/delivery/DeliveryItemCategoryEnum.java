package com.homi.common.lib.enums.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "交割单项目分类枚举")
public enum DeliveryItemCategoryEnum {
    FACILITY("FACILITY", "房间设施"),
    UTILITY("UTILITY", "水电燃气");

    private final String code;
    private final String name;
}
