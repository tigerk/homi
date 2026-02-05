package com.homi.common.lib.enums.sys.todo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 待办优先级枚举
 */
@Getter
@AllArgsConstructor
public enum SysTodoPriorityEnum {
    /**
     * 高
     */
    HIGH(1, "高"),

    /**
     * 中
     */
    MEDIUM(2, "中"),

    /**
     * 低
     */
    LOW(3, "低");

    private final Integer code;
    private final String name;

    public static SysTodoPriorityEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysTodoPriorityEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        SysTodoPriorityEnum type = getByCode(code);
        return type != null ? type.getName() : "未知";
    }
}
