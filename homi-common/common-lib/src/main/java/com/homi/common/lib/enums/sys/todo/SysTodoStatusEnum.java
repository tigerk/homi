package com.homi.common.lib.enums.sys.todo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 待办状态枚举
 */
@Getter
@AllArgsConstructor
public enum SysTodoStatusEnum {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 已处理
     */
    DONE(1, "已处理"),

    /**
     * 已忽略
     */
    IGNORED(2, "已忽略"),

    /**
     * 已过期
     */
    EXPIRED(3, "已过期");

    private final Integer code;
    private final String name;

    public static SysTodoStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysTodoStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        SysTodoStatusEnum type = getByCode(code);
        return type != null ? type.getName() : "未知";
    }
}
