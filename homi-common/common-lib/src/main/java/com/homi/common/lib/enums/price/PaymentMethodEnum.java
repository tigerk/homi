package com.homi.common.lib.enums.price;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethodEnum {
    /**
     * 支付模式
     */
    RENT(0, "随房租付"),
    ALL(1, "一次性全支付"),
    MONTH(2, "月付"),
    BI_MONTH(3, "2月付"),
    QUARTER(4, "季付"),
    HALF_YEAR(5, "半年付"),
    YEAR(6, "年付"),
    ;

    private final Integer code;

    private final String name;


}
