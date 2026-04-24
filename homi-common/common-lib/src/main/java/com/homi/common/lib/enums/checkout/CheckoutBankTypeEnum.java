package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckoutBankTypeEnum {
    UNIONPAY("UNIONPAY", "银联"),
    ALIPAY("ALIPAY", "支付宝"),
    WECHAT("WECHAT", "微信");

    private final String code;
    private final String name;
}
