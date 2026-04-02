package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主包租折算方式枚举")
public enum OwnerProrateTypeEnum {
    BY_DAYS("BY_DAYS", "按天折算"),
    FULL_PERIOD("FULL_PERIOD", "整期计费");

    private final String code;
    private final String name;
}
