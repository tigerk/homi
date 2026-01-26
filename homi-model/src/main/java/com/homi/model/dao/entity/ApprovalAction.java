package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 审批动作表
 * </p>
 *
 * @author tk
 * @since 2026-01-26
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("approval_action")
@Schema(name = "ApprovalAction", description = "审批动作表")
public class ApprovalAction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "实例ID")
    @TableField("instance_id")
    private Long instanceId;

    @Schema(description = "节点ID")
    @TableField("node_id")
    private Long nodeId;

    @Schema(description = "节点序号")
    @TableField("node_order")
    private Integer nodeOrder;

    @Schema(description = "节点名称（冗余）")
    @TableField("node_name")
    private String nodeName;

    @Schema(description = "审批人ID")
    @TableField("approver_id")
    private Long approverId;

    @Schema(description = "审批人姓名（冗余）")
    @TableField("approver_name")
    private String approverName;

    @Schema(description = "操作：1=通过，2=驳回，3=转交")
    @TableField("action")
    private Integer action;

    @Schema(description = "审批意见")
    @TableField("remark")
    private String remark;

    @Schema(description = "转交目标人ID")
    @TableField("transfer_to_id")
    private Long transferToId;

    @Schema(description = "操作时间")
    @TableField("operate_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;

    @Schema(description = "状态：0=待审批，1=已审批，2=已跳过")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
