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
 * 审批节点配置表
 * </p>
 *
 * @author tk
 * @since 2026-01-26
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("approval_node")
@Schema(name = "ApprovalNode", description = "审批节点配置表")
public class ApprovalNode implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "节点ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "流程ID")
    @TableField("flow_id")
    private Long flowId;

    @Schema(description = "节点名称（如：部门经理审批、财务审批）")
    @TableField("node_name")
    private String nodeName;

    @Schema(description = "节点顺序（从1开始）")
    @TableField("node_order")
    private Integer nodeOrder;

    @Schema(description = "审批人类型：1=指定用户，2=指定角色，3=部门主管，4=发起人自选")
    @TableField("approver_type")
    private Integer approverType;

    @Schema(description = "审批人ID列表（用户ID或角色ID）")
    @TableField("approver_ids")
    private String approverIds;

    @Schema(description = "多人审批方式：1=或签（一人通过即可），2=会签（所有人通过）")
    @TableField("multi_approve_type")
    private Integer multiApproveType;

    @Schema(description = "是否删除")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
