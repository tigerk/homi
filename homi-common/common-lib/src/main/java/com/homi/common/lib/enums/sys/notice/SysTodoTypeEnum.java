package com.homi.common.lib.enums.sys.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 待办类型枚举
 */
@Getter
@AllArgsConstructor
public enum SysTodoTypeEnum {
    /**
     * 租约到期
     */
    CONTRACT_EXPIRE(1, "租约到期"),

    /**
     * 账单催收
     */
    BILL_COLLECTION(2, "账单催收"),

    /**
     * 报修处理
     */
    REPAIR_HANDLE(3, "报修处理"),

    /**
     * 合同续签
     */
    CONTRACT_RENEW(4, "合同续签"),

    /**
     * 退房办理
     */
    CHECKOUT_HANDLE(5, "退房办理"),

    /**
     * 其他
     */
    OTHER(6, "其他");

    private final Integer code;
    private final String name;

    public static SysTodoTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysTodoTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        SysTodoTypeEnum type = getByCode(code);
        return type != null ? type.getName() : "未知";
    }
}
