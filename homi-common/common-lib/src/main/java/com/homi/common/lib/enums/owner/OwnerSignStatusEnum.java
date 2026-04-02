package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主合同签署状态枚举")
public enum OwnerSignStatusEnum {
    PENDING(0, "待签字"),
    SIGNED(1, "已签字");

    private final Integer code;
    private final String name;
}
