package com.homi.service.service.approval.event;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 审批状态变更事件
 * <p>
 * Spring Modulith 要求：
 * 1. 实现 Serializable（用于持久化）
 * 2. 提供 getter 方法（用于序列化）
 * 3. 最好是不可变对象（final 字段）
 */
@Getter
@ToString
public class ApprovalStatusChangeEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String bizType;
    private final Long bizId;
    private final Integer approvalStatus;
    private final Instant occurredAt;

    public ApprovalStatusChangeEvent(String bizType, Long bizId, Integer approvalStatus) {
        this.bizType = bizType;
        this.bizId = bizId;
        this.approvalStatus = approvalStatus;
        this.occurredAt = Instant.now();
    }
}
