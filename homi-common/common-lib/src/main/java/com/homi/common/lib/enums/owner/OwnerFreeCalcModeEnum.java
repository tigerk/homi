package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主免租计算方式枚举")
public enum OwnerFreeCalcModeEnum {
    BY_DAYS("BY_DAYS", "按天分摊"),
    FIXED("FIXED", "固定金额"),
    RATIO("RATIO", "按比例");

    private final String code;
    private final String name;
}
