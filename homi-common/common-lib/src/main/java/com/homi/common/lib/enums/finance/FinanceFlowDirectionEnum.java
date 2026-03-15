package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinanceFlowDirectionEnum {
    IN("IN", "收入"),
    OUT("OUT", "支出");

    private final String code;
    private final String label;
}
