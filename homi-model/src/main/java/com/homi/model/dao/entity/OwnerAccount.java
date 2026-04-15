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
 * 业主账户表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_account")
@Schema(name = "OwnerAccount", description = "业主账户表")
public class OwnerAccount implements Serializable {
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

    @Schema(description = "账户状态：1=启用，0=禁用")
    @TableField("account_status")
    private Integer accountStatus;

    @Schema(description = "可用金额")
    @TableField("available_amount")
    private BigDecimal availableAmount;

    @Schema(description = "冻结金额")
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    @Schema(description = "待结算金额")
    @TableField("pending_settlement_amount")
    private BigDecimal pendingSettlementAmount;

    @Schema(description = "累计收入")
    @TableField("total_income_amount")
    private BigDecimal totalIncomeAmount;

    @Schema(description = "累计扣减")
    @TableField("total_reduction_amount")
    private BigDecimal totalReductionAmount;

    @Schema(description = "累计提现")
    @TableField("total_withdraw_amount")
    private BigDecimal totalWithdrawAmount;

    @Schema(description = "版本号")
    @TableField("version")
    private Long version;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
