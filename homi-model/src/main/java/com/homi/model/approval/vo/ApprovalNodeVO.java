package com.homi.model.approval.vo;

import lombok.Data;

import java.util.List;

/**
 * 审批节点 VO
 */
@Data
public class ApprovalNodeVO {

    private Long id;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点顺序
     */
    private Integer nodeOrder;

    /**
     * 审批人类型：1=指定用户，2=指定角色，3=部门主管，4=发起人自选
     */
    private Integer approverType;

    /**
     * 审批人类型名称
     */
    private String approverTypeName;

    /**
     * 审批人ID列表
     */
    private List<Long> approverIds;

    /**
     * 审批人名称列表
     */
    private List<String> approverNames;

    /**
     * 多人审批方式：1=或签，2=会签
     */
    private Integer multiApproveType;

    /**
     * 多人审批方式名称
     */
    private String multiApproveTypeName;
}
