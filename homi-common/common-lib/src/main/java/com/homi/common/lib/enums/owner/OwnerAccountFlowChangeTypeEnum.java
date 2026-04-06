package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主账户流水变动类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主账户流水变动类型枚举")
public enum OwnerAccountFlowChangeTypeEnum {
    BILL_SETTLE_IN("BILL_SETTLE_IN", "账单入账");

    private final String code;
    private final String name;
}
