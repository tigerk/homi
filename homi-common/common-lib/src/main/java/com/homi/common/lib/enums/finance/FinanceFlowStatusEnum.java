package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinanceFlowStatusEnum {
    PENDING(0, "入账中"),
    SUCCESS(1, "已入账"),
    VOIDED(2, "已作废");

    private final Integer code;
    private final String label;
}
