package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主账户流水业务类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主账户流水业务类型枚举")
public enum OwnerAccountFlowBizTypeEnum {
    OWNER_BILL("OWNER_BILL", "业主账单");

    private final String code;
    private final String name;
}
