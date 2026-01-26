package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 审批实例表
 * </p>
 *
 * @author tk
 * @since 2026-01-26
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("approval_instance")
@Schema(name = "ApprovalInstance", description = "审批实例表")
public class ApprovalInstance implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "实例ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "审批单号")
    @TableField("instance_no")
    private String instanceNo;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "流程ID")
    @TableField("flow_id")
    private Long flowId;

    @Schema(description = "业务类型")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "业务ID（如 tenant_checkout.id）")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "业务单号（冗余，便于展示）")
    @TableField("biz_code")
    private String bizCode;

    @Schema(description = "审批标题")
    @TableField("title")
    private String title;

    @Schema(description = "申请人ID")
    @TableField("applicant_id")
    private Long applicantId;

    @Schema(description = "申请人姓名（冗余）")
    @TableField("applicant_name")
    private String applicantName;

    @Schema(description = "当前节点ID")
    @TableField("current_node_id")
    private Long currentNodeId;

    @Schema(description = "当前节点序号")
    @TableField("current_node_order")
    private Integer currentNodeOrder;

    @Schema(description = "状态：0=待提交，1=审批中，2=已通过，3=已驳回，4=已撤回，5=已取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "最终审批意见")
    @TableField("result_remark")
    private String resultRemark;

    @Schema(description = "完成时间")
    @TableField("finish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;

    @Schema(description = "是否删除")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
