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
 * 业主合同主表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_contract")
@Schema(name = "OwnerContract", description = "业主合同主表")
public class OwnerContract implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业主ID")
    @TableField("owner_id")
    private Long ownerId;

    @Schema(description = "合作模式：LIGHT_MANAGED=轻托管，MASTER_LEASE=包租")
    @TableField("cooperation_mode")
    private String cooperationMode;

    @Schema(description = "合同编号")
    @TableField("contract_no")
    private String contractNo;

    @Schema(description = "合同模板ID")
    @TableField("contract_template_id")
    private Long contractTemplateId;

    @Schema(description = "合同内容快照")
    @TableField("contract_content")
    private String contractContent;

    @Schema(description = "签署状态：0=待签字，1=已签字")
    @TableField("sign_status")
    private Integer signStatus;

    @Schema(description = "签约类型")
    @TableField("sign_type")
    private String signType;

    @Schema(description = "合同介质")
    @TableField("contract_medium")
    private String contractMedium;

    @Schema(description = "是否通知业主")
    @TableField("notify_owner")
    private Boolean notifyOwner;

    @Schema(description = "合同开始日期")
    @TableField("contract_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date contractStart;

    @Schema(description = "合同结束日期")
    @TableField("contract_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date contractEnd;

    @Schema(description = "状态：1=启用，0=禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回")
    @TableField("approval_status")
    private Integer approvalStatus;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除：0=否，1=是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
