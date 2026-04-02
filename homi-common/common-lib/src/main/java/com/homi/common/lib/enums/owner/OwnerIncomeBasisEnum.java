package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主结算收入口径枚举")
public enum OwnerIncomeBasisEnum {
    RECEIVED("RECEIVED", "按实收"),
    RECEIVABLE("RECEIVABLE", "按应收");

    private final String code;
    private final String name;
}
