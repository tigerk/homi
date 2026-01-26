package com.homi.service.service.approval;

import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import lombok.Builder;
import lombok.Data;

/**
 * 审批执行结果
 */
@Data
@Builder
public class ApprovalResult {

    /**
     * 是否需要审批
     */
    private boolean needApproval;

    /**
     * 审批实例ID（仅当需要审批时有值）
     */
    private Long instanceId;

    /**
     * 审批状态：1-审批中(PENDING) 2-已通过(APPROVED)
     */
    private Integer approvalStatus;

    /**
     * 是否直接通过（无需审批）
     */
    public boolean isApproved() {
        return !needApproval;
    }

    /**
     * 是否在审批中
     */
    public boolean isPending() {
        return needApproval;
    }

    /**
     * 需要审批 - 状态为 PENDING
     */
    public static ApprovalResult pending(Long instanceId) {
        return ApprovalResult.builder()
            .needApproval(true)
            .instanceId(instanceId)
            .approvalStatus(BizApprovalStatusEnum.PENDING.getCode())
            .build();
    }

    /**
     * 无需审批 - 状态为 APPROVED
     */
    public static ApprovalResult approved() {
        return ApprovalResult.builder()
            .needApproval(false)
            .approvalStatus(BizApprovalStatusEnum.APPROVED.getCode())
            .build();
    }
}
