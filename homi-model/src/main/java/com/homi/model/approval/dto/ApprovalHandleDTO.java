package com.homi.model.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 处理审批 DTO（通过/驳回/转交）
 */
@Data
@Schema(description = "处理审批 DTO")
public class ApprovalHandleDTO {

    /**
     * 审批实例ID
     */
    @Schema(description = "审批实例ID")
    @NotNull(message = "审批实例ID不能为空")
    private Long instanceId;

    /**
     * 审批人ID
     */
    @Schema(description = "审批人ID")
    private Long approverId;

    /**
     * 操作类型：1=通过，2=驳回，3=转交
     * @see com.homi.common.lib.enums.approval.ApprovalActionTypeEnum
     */
    @Schema(description = "操作类型：1=通过，2=驳回，3=转交")
    @NotNull(message = "操作类型不能为空")
    private Integer action;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见")
    private String remark;

    /**
     * 转交目标人ID（action=3 时必填）
     */
    @Schema(description = "转交目标人ID（action=3 时必填）")
    private Long transferToId;
}
