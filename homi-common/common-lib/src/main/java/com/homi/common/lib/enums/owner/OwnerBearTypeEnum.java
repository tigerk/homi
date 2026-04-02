package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主承担方枚举")
public enum OwnerBearTypeEnum {
    PLATFORM("PLATFORM", "平台承担"),
    OWNER("OWNER", "业主承担"),
    SHARED("SHARED", "共同承担");

    private final String code;
    private final String name;
}
