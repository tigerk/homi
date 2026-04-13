package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主账单业务类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主账单业务类型枚举")
public enum OwnerBillBizTypeEnum {
    LIGHT_MANAGED_SETTLEMENT("LIGHT_MANAGED_SETTLEMENT", "轻托管结算单"),
    MASTER_LEASE_PAYABLE("MASTER_LEASE_PAYABLE", "包租应付单");

    private final String code;
    private final String name;

    public static OwnerBillBizTypeEnum fromCode(String code) {
        for (OwnerBillBizTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
