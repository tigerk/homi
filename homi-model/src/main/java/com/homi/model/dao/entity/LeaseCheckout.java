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
 * 退租主表（退租并结账）
 *
 * @author tk
 * @since 2026-02-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease_checkout")
@Schema(name = "LeaseCheckout", description = "退租主表")
public class LeaseCheckout implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "退租单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "退租单编号")
    @TableField("checkout_code")
    private String checkoutCode;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "租客ID")
    @TableField("tenant_id")
    private Long tenantId;

    @Schema(description = "租约ID")
    @TableField("lease_id")
    private Long leaseId;

    @Schema(description = "交割单ID（关联 delivery.id，handover_type=CHECK_OUT）")
    @TableField("delivery_id")
    private Long deliveryId;

    @Schema(description = "退租类型：1=正常退，2=违约退")
    @TableField("checkout_type")
    private Integer checkoutType;

    @Schema(description = "解约原因（违约退时选填）")
    @TableField("breach_reason")
    private String breachReason;

    @Schema(description = "合同到期日")
    @TableField("lease_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaseEnd;

    @Schema(description = "实际离房日期")
    @TableField("actual_checkout_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date actualCheckoutDate;

    @Schema(description = "押金总额")
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    @Schema(description = "收入总额（租客应付）")
    @TableField("income_amount")
    private BigDecimal incomeAmount;

    @Schema(description = "支出总额（退还租客）")
    @TableField("expense_amount")
    private BigDecimal expenseAmount;

    @Schema(description = "最终结算（负数=应退租客，正数=租客补缴）")
    @TableField("final_amount")
    private BigDecimal finalAmount;

    @Schema(description = "预计收/付款时间")
    @TableField("expected_payment_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectedPaymentDate;

    @Schema(description = "账单处理方式：1=生成待付账单，2=线下付款，3=申请付款，4=标记坏账")
    @TableField("settlement_method")
    private Integer settlementMethod;

    @Schema(description = "坏账原因（标记坏账时必填）")
    @TableField("bad_debt_reason")
    private String badDebtReason;

    @Schema(description = "状态：0=草稿，1=待确认，2=已完成，3=已取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回")
    @TableField("approval_status")
    private Integer approvalStatus;

    @Schema(description = "结算完成时间")
    @TableField("settlement_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date settlementTime;

    @Schema(description = "退租备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "退租凭证附件ID列表（JSON数组）")
    @TableField("attachment_ids")
    private String attachmentIds;

    // ===== 收款人信息 =====

    @Schema(description = "收款人姓名")
    @TableField("payee_name")
    private String payeeName;

    @Schema(description = "收款人电话")
    @TableField("payee_phone")
    private String payeePhone;

    @Schema(description = "收款人证件类型")
    @TableField("payee_id_type")
    private String payeeIdType;

    @Schema(description = "收款人证件号")
    @TableField("payee_id_number")
    private String payeeIdNumber;

    @Schema(description = "银行类型（银联等）")
    @TableField("bank_type")
    private String bankType;

    @Schema(description = "银行卡类型（借记卡/信用卡）")
    @TableField("bank_card_type")
    private String bankCardType;

    @Schema(description = "银行账号")
    @TableField("bank_account")
    private String bankAccount;

    @Schema(description = "银行名称")
    @TableField("bank_name")
    private String bankName;

    @Schema(description = "支行名称")
    @TableField("bank_branch")
    private String bankBranch;

    @Schema(description = "是否发送退租确认单")
    @TableField("send_confirmation")
    private Boolean sendConfirmation;

    @Schema(description = "退租确认单模板")
    @TableField("confirmation_template")
    private String confirmationTemplate;

    // ===== 通用字段 =====

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
