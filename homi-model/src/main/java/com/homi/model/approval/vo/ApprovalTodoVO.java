package com.homi.model.approval.vo;

import com.homi.model.tenant.vo.TenantDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 审批待办/已办 VO
 */
@Data
@Schema(description = "审批待办/已办 VO")
public class ApprovalTodoVO {

    /**
     * 动作ID
     */
    @Schema(description = "动作ID")
    private Long actionId;

    /**
     * 实例ID
     */
    @Schema(description = "实例ID")
    private Long instanceId;

    /**
     * 审批单号
     */
    @Schema(description = "审批单号")
    private String instanceNo;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 业务类型名称
     */
    @Schema(description = "业务类型名称")
    private String bizTypeName;

    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Long bizId;

    /**
     * 业务单号
     */
    @Schema(description = "业务单号")
    private String bizCode;

    /**
     * 审批标题
     */
    @Schema(description = "审批标题")
    private String title;

    /**
     * 申请人姓名
     */
    @Schema(description = "申请人姓名")
    private String applicantName;

    /**
     * 当前节点名称
     */
    @Schema(description = "当前节点名称")
    private String nodeName;

    /**
     * 节点顺序
     */
    @Schema(description = "节点顺序")
    private Integer nodeOrder;

    /**
     * 申请时间
     */
    @Schema(description = "申请时间")
    private Date applyTime;

    /**
     * 操作：1=通过，2=驳回（已办时显示）
     */
    @Schema(description = "操作：1=通过，2=驳回（已办时显示）")
    private Integer action;

    /**
     * 操作名称
     */
    @Schema(description = "操作名称")
    private String actionName;

    /**
     * 审批意见（已办时显示）
     */
    @Schema(description = "审批意见（已办时显示）")
    private String remark;

    /**
     * 操作时间（已办时显示）
     */
    @Schema(description = "操作时间（已办时显示）")
    private Date operateTime;

    /**
     * 实例状态
     */
    @Schema(description = "实例状态")
    private Integer instanceStatus;

    /**
     * 实例状态名称
     */
    @Schema(description = "实例状态名称")
    private String instanceStatusName;

    /**
     * 租户详情
     */
    @Schema(description = "租户详情")
    private TenantDetailVO tenantDetail;
}
