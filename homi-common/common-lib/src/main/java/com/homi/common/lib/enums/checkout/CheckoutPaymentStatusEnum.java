package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckoutPaymentStatusEnum {
    UNPAID("UNPAID", "待支付"),
    PAID("PAID", "已支付"),
    NO_PAYMENT_REQUIRED("NO_PAYMENT_REQUIRED", "无需支付");

    private final String code;
    private final String name;
}
