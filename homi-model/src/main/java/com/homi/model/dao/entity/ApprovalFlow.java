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
 * 审批流程配置表
 * </p>
 *
 * @author tk
 * @since 2026-01-26
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("approval_flow")
@Schema(name = "ApprovalFlow", description = "审批流程配置表")
public class ApprovalFlow implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "流程ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "流程编码（唯一标识）")
    @TableField("flow_code")
    private String flowCode;

    @Schema(description = "流程名称")
    @TableField("flow_name")
    private String flowName;

    @Schema(description = "业务类型：TENANT_CHECKIN=租客入住，TENANT_CHECKOUT=退租，HOUSE_CREATE=房源录入，CONTRACT_SIGN=合同签署")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "是否启用：false=停用，true=启用")
    @TableField("enabled")
    private Boolean enabled;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

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
