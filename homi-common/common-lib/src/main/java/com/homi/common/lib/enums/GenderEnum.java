package com.homi.common.lib.enums;

/**
 * 性别枚举
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:22
 */
public enum GenderEnum {

    /**
     * 未知
     */
    UNKNOWN(0),

    /**
     * 男
     */
    MALE(1),

    /**
     * 女
     */
    FEMALE(2);

    private final int value;

    GenderEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
