package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentFlowBizTypeEnum {
    LEASE_BILL("LEASE_BILL", "租客账单");

    private final String code;
    private final String label;
}
