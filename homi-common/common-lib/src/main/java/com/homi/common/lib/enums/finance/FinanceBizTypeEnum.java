package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinanceBizTypeEnum {
    LEASE_BILL_FEE("LEASE_BILL_FEE", "租客账单费用项");

    private final String code;
    private final String label;
}
