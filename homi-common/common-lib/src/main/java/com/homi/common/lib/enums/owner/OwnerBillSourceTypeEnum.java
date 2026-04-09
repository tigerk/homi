package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主账单来源类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主账单来源类型枚举")
public enum OwnerBillSourceTypeEnum {
    OWNER_CONTRACT_SUBJECT("OWNER_CONTRACT_SUBJECT", "业主合同房源"),
    OWNER_CONTRACT("OWNER_CONTRACT", "业主合同"),
    OWNER_LEASE_FEE("OWNER_LEASE_FEE", "包租其他费用"),
    OWNER_LEASE_FREE_RULE("OWNER_LEASE_FREE_RULE", "包租免租规则");

    private final String code;
    private final String name;
}
