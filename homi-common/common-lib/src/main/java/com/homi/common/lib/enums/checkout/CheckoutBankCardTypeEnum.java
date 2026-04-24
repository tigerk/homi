package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckoutBankCardTypeEnum {
    DEBIT("DEBIT", "借记卡"),
    CREDIT("CREDIT", "信用卡");

    private final String code;
    private final String name;
}
