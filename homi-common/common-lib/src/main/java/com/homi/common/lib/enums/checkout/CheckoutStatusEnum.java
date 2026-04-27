package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退租单状态枚举
 */
@Getter
@AllArgsConstructor
public enum CheckoutStatusEnum {

    DRAFT(0, "草稿"),
    PENDING(1, "待确认"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    private final Integer code;
    private final String name;

    public static CheckoutStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (CheckoutStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        CheckoutStatusEnum e = getByCode(code);
        return e != null ? e.getName() : "";
    }
}
