package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主费用计算方式枚举")
public enum OwnerFeeModeEnum {
    RATIO("RATIO", "按比例"),
    FIXED("FIXED", "固定金额");

    private final String code;
    private final String name;
}
