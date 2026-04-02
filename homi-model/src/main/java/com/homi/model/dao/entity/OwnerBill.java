package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * 业主账单主表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_bill")
@Schema(name = "OwnerBill", description = "业主账单主表")
public class OwnerBill implements Serializable {
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

    @Schema(description = "业主合同ID")
    @TableField("contract_id")
    private Long contractId;

    @Schema(description = "账单编号")
    @TableField("bill_no")
    private String billNo;

    @Schema(description = "账单开始日期")
    @TableField("bill_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账单结束日期")
    @TableField("bill_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "收入金额")
    @TableField("income_amount")
    private BigDecimal incomeAmount;

    @Schema(description = "减免金额")
    @TableField("reduction_amount")
    private BigDecimal reductionAmount;

    @Schema(description = "费用金额")
    @TableField("expense_amount")
    private BigDecimal expenseAmount;

    @Schema(description = "调整金额")
    @TableField("adjust_amount")
    private BigDecimal adjustAmount;

    @Schema(description = "应付金额")
    @TableField("payable_amount")
    private BigDecimal payableAmount;

    @Schema(description = "已结金额")
    @TableField("settled_amount")
    private BigDecimal settledAmount;

    @Schema(description = "已提现金额")
    @TableField("withdrawn_amount")
    private BigDecimal withdrawnAmount;

    @Schema(description = "冻结金额")
    @TableField("freeze_amount")
    private BigDecimal freezeAmount;

    @Schema(description = "可提现金额")
    @TableField("withdrawable_amount")
    private BigDecimal withdrawableAmount;

    @Schema(description = "账单状态")
    @TableField("bill_status")
    private Integer billStatus;

    @Schema(description = "审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回")
    @TableField("approval_status")
    private Integer approvalStatus;

    @Schema(description = "结算状态")
    @TableField("settlement_status")
    private Integer settlementStatus;

    @Schema(description = "生成时间")
    @TableField("generated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date generatedAt;

    @Schema(description = "审批通过时间")
    @TableField("approved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approvedAt;

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
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
