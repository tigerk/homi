package com.homi.common.lib.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentFlowStatusEnum {
    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败"),
    CLOSED(3, "已关闭"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "已退款");

    private final Integer code;
    private final String label;
}
