package com.homi.common.lib.enums.sys.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 个人消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum SysMessageTypeEnum {
    /**
     * 系统消息
     */
    SYSTEM(1, "系统消息"),

    /**
     * 租约提醒
     */
    CONTRACT_REMIND(2, "租约提醒"),

    /**
     * 缴费提醒
     */
    BILL_REMIND(3, "缴费提醒"),

    /**
     * 报修通知
     */
    REPAIR_NOTIFY(4, "报修通知"),

    /**
     * 私信
     */
    PRIVATE_CHAT(5, "私信");

    private final Integer code;
    private final String name;

    public static SysMessageTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysMessageTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        SysMessageTypeEnum type = getByCode(code);
        return type != null ? type.getName() : "未知";
    }
}
