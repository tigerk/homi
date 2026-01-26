package com.homi.model.approval.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 审批流程保存 DTO
 */
@Data
public class ApprovalFlowDTO {

    /**
     * 流程ID（修改时传入）
     */
    private Long id;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    private String flowName;

    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审批节点列表
     */
    @NotNull(message = "审批节点不能为空")
    private List<ApprovalNodeDTO> nodes;

    /**
     * 创建人
     */
    private Long createBy;
}
