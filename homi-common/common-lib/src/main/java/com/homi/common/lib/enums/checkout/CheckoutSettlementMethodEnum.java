package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退租账单处理方式枚举
 */
@Getter
@AllArgsConstructor
public enum CheckoutSettlementMethodEnum {

    GENERATE_BILL(1, "生成待付账单"),
    OFFLINE_PAYMENT(2, "线下付款"),
    APPLY_PAYMENT(3, "申请付款"),
    BAD_DEBT(4, "标记坏账");

    private final Integer code;
    private final String name;

    public static CheckoutSettlementMethodEnum getByCode(Integer code) {
        if (code == null) return null;
        for (CheckoutSettlementMethodEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        CheckoutSettlementMethodEnum e = getByCode(code);
        return e != null ? e.getName() : "";
    }
}
