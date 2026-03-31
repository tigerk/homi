package com.homi.common.lib.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:22
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女");

    private final int code;

    private final String name;
}
