package com.homi.model.approval.vo;

import lombok.Data;
import java.util.Date;

/**
 * 审批待办/已办 VO
 */
@Data
public class ApprovalTodoVO {

    /**
     * 动作ID
     */
    private Long actionId;

    /**
     * 实例ID
     */
    private Long instanceId;

    /**
     * 审批单号
     */
    private String instanceNo;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务类型名称
     */
    private String bizTypeName;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 业务单号
     */
    private String bizCode;

    /**
     * 审批标题
     */
    private String title;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 当前节点名称
     */
    private String nodeName;

    /**
     * 节点顺序
     */
    private Integer nodeOrder;

    /**
     * 申请时间
     */
    private Date applyTime;

    /**
     * 操作：1=通过，2=驳回（已办时显示）
     */
    private Integer action;

    /**
     * 操作名称
     */
    private String actionName;

    /**
     * 审批意见（已办时显示）
     */
    private String remark;

    /**
     * 操作时间（已办时显示）
     */
    private Date operateTime;

    /**
     * 实例状态
     */
    private Integer instanceStatus;

    /**
     * 实例状态名称
     */
    private String instanceStatusName;
}
