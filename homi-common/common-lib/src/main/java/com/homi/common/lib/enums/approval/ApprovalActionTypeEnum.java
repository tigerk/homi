package com.homi.common.lib.enums.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批操作类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApprovalActionTypeEnum {
    /**
     * 通过
     */
    APPROVE(1, "通过"),

    /**
     * 驳回
     */
    REJECT(2, "驳回"),

    /**
     * 转交
     */
    TRANSFER(3, "转交");

    private final Integer code;
    private final String name;

    public static ApprovalActionTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalActionTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        ApprovalActionTypeEnum actionType = getByCode(code);
        return actionType != null ? actionType.getName() : "未知";
    }
}
