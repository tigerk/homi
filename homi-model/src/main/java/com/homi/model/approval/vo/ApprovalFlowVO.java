package com.homi.model.approval.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 审批流程 VO
 */
@Data
public class ApprovalFlowVO {

    private Long id;

    /**
     * 流程编码
     */
    private String flowCode;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务类型名称
     */
    private String bizTypeName;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 审批节点列表
     */
    private List<ApprovalNodeVO> nodes;
}
