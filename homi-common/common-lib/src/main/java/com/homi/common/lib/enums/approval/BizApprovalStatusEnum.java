package com.homi.common.lib.enums.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务审批状态枚举
 * <p>
 * 用于业务表的 approval_status 字段
 * <p>
 * 状态流转：
 * - 需要审批：PENDING -> APPROVED/REJECTED/WITHDRAWN
 * - 无需审批：直接设置为 APPROVED
 */
@Getter
@AllArgsConstructor
public enum BizApprovalStatusEnum {

    /**
     * 审批中
     */
    PENDING(1, "审批中"),

    /**
     * 已通过（包含"无需审批直接通过"的情况）
     */
    APPROVED(2, "已通过"),

    /**
     * 已驳回
     */
    REJECTED(3, "已驳回"),

    /**
     * 已撤回
     */
    WITHDRAWN(4, "已撤回");

    private final Integer code;
    private final String name;

    public static BizApprovalStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (BizApprovalStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 是否在审批中
     */
    public static boolean isPending(Integer code) {
        return PENDING.getCode().equals(code);
    }

    /**
     * 是否已通过
     */
    public static boolean isApproved(Integer code) {
        return APPROVED.getCode().equals(code);
    }

    /**
     * 是否可以重新提交（驳回或撤回后）
     */
    public static boolean canResubmit(Integer code) {
        return REJECTED.getCode().equals(code) || WITHDRAWN.getCode().equals(code);
    }
}
