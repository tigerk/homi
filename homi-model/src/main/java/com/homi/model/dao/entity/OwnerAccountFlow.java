package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 业主账户流水表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_account_flow")
@Schema(name = "OwnerAccountFlow", description = "业主账户流水表")
public class OwnerAccountFlow implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业主ID")
    @TableField("owner_id")
    private Long ownerId;

    @Schema(description = "业务类型")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "业务ID")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "流水方向")
    @TableField("flow_direction")
    private String flowDirection;

    @Schema(description = "变动类型")
    @TableField("change_type")
    private String changeType;

    @Schema(description = "变动金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "变动前可用金额")
    @TableField("available_before")
    private BigDecimal availableBefore;

    @Schema(description = "变动后可用金额")
    @TableField("available_after")
    private BigDecimal availableAfter;

    @Schema(description = "变动前冻结金额")
    @TableField("frozen_before")
    private BigDecimal frozenBefore;

    @Schema(description = "变动后冻结金额")
    @TableField("frozen_after")
    private BigDecimal frozenAfter;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;
}
