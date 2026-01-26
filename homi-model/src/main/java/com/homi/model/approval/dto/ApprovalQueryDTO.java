package com.homi.model.approval.dto;

import com.homi.common.lib.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApprovalQueryDTO extends PageDTO {

    /**
     * 流程ID（查询流程详情、删除、切换状态时使用）
     */
    private Long flowId;

    /**
     * 实例ID（查询实例详情、撤回时使用）
     */
    private Long instanceId;

    /**
     * 业务ID（查询业务审批实例时使用）
     */
    private Long bizId;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 申请人ID（我发起的）
     */
    private Long applicantId;

    /**
     * 审批人ID（我待办/已办）
     */
    private Long approverId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 审批状态
     */
    private Integer status;

    /**
     * 关键字搜索（标题/单号）
     */
    private String keyword;
}
