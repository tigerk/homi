package com.homi.model.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

/**
 * 提交审批 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalSubmitDTO {

    /**
     * 公司ID
     */
    @NotNull(message = "公司ID不能为空")
    private Long companyId;

    /**
     * 业务类型
     * @see com.homi.common.lib.enums.approval.ApprovalBizTypeEnum
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 业务ID（如 lease_checkout.id）
     */
    @NotNull(message = "业务ID不能为空")
    private Long bizId;

    /**
     * 审批标题
     */
    @NotBlank(message = "审批标题不能为空")
    private String title;

    /**
     * 申请人ID
     */
    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;

    /**
     * 申请备注
     */
    private String remark;
}
