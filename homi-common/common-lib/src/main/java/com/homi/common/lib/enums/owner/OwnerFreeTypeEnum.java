package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主免租类型枚举")
public enum OwnerFreeTypeEnum {
    BUILT_IN("BUILT_IN", "内置免租"),
    OUTSIDE("OUTSIDE", "外置免租");

    private final String code;
    private final String name;
}
