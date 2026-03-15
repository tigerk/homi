package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentFlowDirectionEnum {
    IN("IN", "入账"),
    OUT("OUT", "出账");

    private final String code;
    private final String label;
}
