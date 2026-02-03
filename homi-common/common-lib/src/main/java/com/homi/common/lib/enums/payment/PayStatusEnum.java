package com.homi.common.lib.enums.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayStatusEnum {
    UNPAID(0, "未支付"),
    PARTIALLY_PAID(1, "部分支付"),
    PAID(2, "已支付"),
    ;

    private final int code;
    private final String name;
}
