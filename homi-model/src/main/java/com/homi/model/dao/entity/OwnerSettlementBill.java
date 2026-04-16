package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_settlement_bill")
@Schema(name = "OwnerSettlementBill", description = "轻托管业主结算单主表")
public class OwnerSettlementBill implements Serializable {
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

    @Schema(description = "业主合同ID")
    @TableField("contract_id")
    private Long contractId;

    @Schema(description = "合同房源类型")
    @TableField("subject_type")
    private String subjectType;

    @Schema(description = "合同房源ID")
    @TableField("subject_id")
    private Long subjectId;

    @Schema(description = "合同房源名称快照")
    @TableField("subject_name_snapshot")
    private String subjectNameSnapshot;

    @Schema(description = "结算单号")
    @TableField("bill_no")
    private String billNo;

    @Schema(description = "账期开始日期")
    @TableField("bill_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStartDate;

    @Schema(description = "账期结束日期")
    @TableField("bill_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEndDate;

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

    @Schema(description = "应结金额")
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

    @Schema(description = "单据状态")
    @TableField("bill_status")
    private Integer billStatus;

    @Schema(description = "审批状态")
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
