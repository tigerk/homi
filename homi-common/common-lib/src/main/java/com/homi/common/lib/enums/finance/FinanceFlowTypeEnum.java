package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinanceFlowTypeEnum {
    RECEIVE("RECEIVE", "收款"),
    PAY("PAY", "付款"),
    REFUND("REFUND", "退款"),
    VOID("VOID", "作废"),
    ADJUST("ADJUST", "调整");

    private final String code;
    private final String label;
}
