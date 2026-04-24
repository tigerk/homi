package com.homi.common.lib.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费用收支方向枚举
 * <p>
 * IN = 收入（租客应付/扣款方向）
 * OUT = 支出（退还租客/退款方向）
 */
@Getter
@AllArgsConstructor
public enum FeeDirectionEnum {

    /**
     * 收入（扣款）
     */
    IN("IN", "收入"),

    /**
     * 支出（退款）
     */
    OUT("OUT", "支出");

    private final String code;
    private final String label;

    public static FeeDirectionEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (FeeDirectionEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getLabelByCode(String code) {
        FeeDirectionEnum e = getByCode(code);
        return e != null ? e.getLabel() : "";
    }

    public boolean isIn() {
        return this == IN;
    }

    public boolean isOut() {
        return this == OUT;
    }
}
