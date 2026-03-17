package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentFlowChannelEnum {
    CASH("CASH", "现金"),
    TRANSFER("TRANSFER", "转账"),
    ALIPAY("ALIPAY", "支付宝"),
    WECHAT("WECHAT", "微信"),
    YEEPAY("YEEPAY", "易宝"),
    POS("POS", "POS"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String label;
}
