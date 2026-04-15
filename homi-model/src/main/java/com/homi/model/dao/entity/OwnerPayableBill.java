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
@TableName("owner_payable_bill")
@Schema(name = "OwnerPayableBill", description = "包租业主应付单主表")
public class OwnerPayableBill implements Serializable {
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

    @Schema(description = "合同房源类型")
    @TableField("subject_type")
    private String subjectType;

    @Schema(description = "合同房源ID")
    @TableField("subject_id")
    private Long subjectId;

    @Schema(description = "合同房源名称快照")
    @TableField("subject_name_snapshot")
    private String subjectNameSnapshot;

    @Schema(description = "应付单号")
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

    @Schema(description = "应付日期")
    @TableField("due_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "应付金额")
    @TableField("payable_amount")
    private BigDecimal payableAmount;

    @Schema(description = "已付金额")
    @TableField("paid_amount")
    private BigDecimal paidAmount;

    @Schema(description = "未付金额")
    @TableField("unpaid_amount")
    private BigDecimal unpaidAmount;

    @Schema(description = "调整金额")
    @TableField("adjust_amount")
    private BigDecimal adjustAmount;

    @Schema(description = "付款状态")
    @TableField("payment_status")
    private Integer paymentStatus;

    @Schema(description = "单据状态")
    @TableField("bill_status")
    private Integer billStatus;

    @Schema(description = "作废原因")
    @TableField("cancel_reason")
    private String cancelReason;

    @Schema(description = "作废操作人ID")
    @TableField("cancel_by")
    private Long cancelBy;

    @Schema(description = "作废操作人名称")
    @TableField("cancel_by_name")
    private String cancelByName;

    @Schema(description = "作废时间")
    @TableField("cancel_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelAt;

    @Schema(description = "生成时间")
    @TableField("generated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date generatedAt;

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
