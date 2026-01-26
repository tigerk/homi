package com.homi.common.lib.enums.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批实例状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatusEnum {

    DRAFT(0, "待提交"),
    PENDING(1, "审批中"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回"),
    WITHDRAWN(4, "已撤回"),
    CANCELLED(5, "已取消"),
    ;

    private final Integer code;
    private final String name;

    public static ApprovalStatusEnum getByCode(Integer code) {
        for (ApprovalStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
