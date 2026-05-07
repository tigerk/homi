package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinanceFlowSourceTypeEnum {
    PAYMENT_FLOW("PAYMENT_FLOW", "租客支付流水"),
    OWNER_PAYABLE_BILL_PAYMENT("OWNER_PAYABLE_BILL_PAYMENT", "包租应付付款");

    private final String code;
    private final String label;
}
