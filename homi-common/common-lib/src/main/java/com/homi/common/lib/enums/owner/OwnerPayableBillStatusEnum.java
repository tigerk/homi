package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "包租业主应付单状态枚举")
public enum OwnerPayableBillStatusEnum {
    NORMAL(1, "正常"),
    CANCELED(2, "已作废");

    private final Integer code;
    private final String name;
}
