package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主签约类型枚举")
public enum OwnerSignTypeEnum {
    NEW("NEW", "新签"),
    RENEW("RENEW", "续签");

    private final String code;
    private final String name;
}
