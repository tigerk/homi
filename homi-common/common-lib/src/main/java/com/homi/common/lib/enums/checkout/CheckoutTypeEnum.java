package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退租类型枚举
 * 只有两种：正常退、违约退
 */
@Getter
@AllArgsConstructor
public enum CheckoutTypeEnum {

    NORMAL(1, "正常退"),
    BREACH(2, "违约退");

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

    /**
     * 是否为正常退租
     */
    public boolean isNormal() {
        return this == NORMAL;
    }

    /**
     * 是否为违约退租（默认不退押金）
     */
    public boolean isBreach() {
        return this == BREACH;
    }
}
