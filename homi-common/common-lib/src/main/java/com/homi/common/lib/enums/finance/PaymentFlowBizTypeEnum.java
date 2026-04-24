package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentFlowBizTypeEnum {
    LEASE_BILL("LEASE_BILL", "租客账单"),
    TENANT_CHECKOUT("TENANT_CHECKOUT", "租客退租");

    private final String code;
    private final String label;
}
