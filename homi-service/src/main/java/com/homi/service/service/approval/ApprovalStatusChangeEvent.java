package com.homi.service.service.approval;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 审批状态变更事件
 */
@Getter
public class ApprovalStatusChangeEvent extends ApplicationEvent {

    private final String bizType;
    private final Long bizId;
    private final Integer approvalStatus;

    public ApprovalStatusChangeEvent(Object source, String bizType, Long bizId, Integer approvalStatus) {
        super(source);
        this.bizType = bizType;
        this.bizId = bizId;
        this.approvalStatus = approvalStatus;
    }
}
