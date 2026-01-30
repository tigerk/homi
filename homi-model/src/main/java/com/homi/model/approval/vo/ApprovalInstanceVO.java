package com.homi.model.approval.vo;

import com.homi.model.tenant.vo.TenantDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 审批实例 VO
 */
@Data
public class ApprovalInstanceVO {

    @Schema(description = "审批实例ID")
    private Long id;

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
     * 申请人ID
     */
    @Schema(description = "申请人ID")
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @Schema(description = "申请人姓名")
    private String applicantName;

    /**
     * 当前节点序号
     */
    @Schema(description = "当前节点序号")
    private Integer currentNodeOrder;

    /**
     * 当前节点名称
     */
    @Schema(description = "当前节点名称")
    private String currentNodeName;

    @Schema(description = "状态：0=待提交，1=审批中，2=已通过，3=已驳回，4=已撤回，5=已取消")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称")
    private String statusName;

    /**
     * 最终审批意见
     */
    @Schema(description = "最终审批意见")
    private String resultRemark;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private Date finishTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 审批动作列表（审批流程时间线）
     */
    @Schema(description = "审批动作列表（审批流程时间线）")
    private List<ApprovalActionVO> actions;

    @Schema(description = "租户详情")
    private TenantDetailVO tenantDetail;
}

