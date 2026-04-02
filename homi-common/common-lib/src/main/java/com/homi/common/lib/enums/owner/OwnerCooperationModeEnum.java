package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主合作模式枚举")
public enum OwnerCooperationModeEnum {
    LIGHT_MANAGED("LIGHT_MANAGED", "轻托管"),
    MASTER_LEASE("MASTER_LEASE", "包租");

    private final String code;
    private final String name;
}
