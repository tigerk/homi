package com.homi.model.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 审批动作 VO
 */
@Data
public class ApprovalActionVO {

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
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 操作：1=通过，2=驳回，3=转交
     */
    private Integer action;

    /**
     * 操作名称
     */
    private String actionName;

    /**
     * 审批意见
     */
    private String remark;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;

    /**
     * 状态：0=待审批，1=已审批，2=已跳过
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;
}
