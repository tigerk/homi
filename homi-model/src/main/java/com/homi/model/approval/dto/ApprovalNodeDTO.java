package com.homi.model.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalNodeDTO {

    /**
     * 节点名称
     */
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    /**
     * 节点顺序
     */
    private Integer nodeOrder;

    /**
     * 审批人类型：1=指定用户，2=指定角色，3=部门主管，4=发起人自选
     */
    @NotNull(message = "审批人类型不能为空")
    private Integer approverType;

    /**
     * 审批人ID列表
     */
    private List<Long> approverIds;

    /**
     * 多人审批方式：1=或签，2=会签
     */
    private Integer multiApproveType = 1;
}
