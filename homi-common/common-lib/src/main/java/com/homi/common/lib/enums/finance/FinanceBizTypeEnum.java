package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinanceBizTypeEnum {
    LEASE_BILL("LEASE_BILL", "租金");

    private final String code;
    private final String label;
}
