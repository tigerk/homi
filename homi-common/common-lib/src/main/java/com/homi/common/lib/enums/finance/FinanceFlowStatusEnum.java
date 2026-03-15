package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinanceFlowStatusEnum {
    PENDING(0, "入账中"),
    SUCCESS(1, "已入账"),
    FAILED(2, "失败"),
    VOIDED(3, "已作废");

    private final Integer code;
    private final String label;
}
