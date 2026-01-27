package com.homi.common.lib.enums.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批实例状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApprovalActionStatusEnum {
    /**
     * 状态：0=待审批，1=已审批，2=已跳过
     */
    PENDING(0, "待审批"),
    APPROVED(1, "已审批"),
    SKIPPED(2, "已跳过"),
    ;

    private final Integer code;
    private final String name;

    public static ApprovalActionStatusEnum getByCode(Integer code) {
        for (ApprovalActionStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
