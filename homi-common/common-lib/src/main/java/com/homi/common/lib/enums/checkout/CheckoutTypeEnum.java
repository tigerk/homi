package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退租类型枚举
 */
@Getter
@AllArgsConstructor
public enum CheckoutTypeEnum {

    NORMAL_EXPIRE(1, "正常到期"),
    EARLY_CHECKOUT(2, "提前退租"),
    ROOM_CHANGE(3, "换房退租"),
    BREACH(4, "违约退租"),
    NEGOTIATION(5, "协商解约");

    private final Integer code;
    private final String name;

    public static CheckoutTypeEnum getByCode(Integer code) {
        if (code == null) return null;
        for (CheckoutTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        CheckoutTypeEnum e = getByCode(code);
        return e != null ? e.getName() : "";
    }
}
