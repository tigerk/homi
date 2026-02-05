package com.homi.common.lib.enums.sys.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统公告类型枚举
 */
@Getter
@AllArgsConstructor
public enum SysNoticeTypeEnum {
    /**
     * 系统公告
     */
    SYSTEM(1, "系统公告"),

    /**
     * 运营通知
     */
    OPERATION(2, "运营通知");

    private final Integer code;
    private final String name;

    public static SysNoticeTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysNoticeTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        SysNoticeTypeEnum type = getByCode(code);
        return type != null ? type.getName() : "未知";
    }
}
