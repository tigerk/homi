package com.homi.common.lib.enums.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批操作枚举
 */
@Getter
@AllArgsConstructor
public enum ApprovalActionEnum {
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回"),
    TRANSFERRED(3, "已转交");

    private final Integer code;
    private final String name;

    public static ApprovalActionEnum getByCode(Integer code) {
        for (ApprovalActionEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
