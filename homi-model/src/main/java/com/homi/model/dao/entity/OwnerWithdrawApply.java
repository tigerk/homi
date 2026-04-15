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
 * 业主提现申请表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_withdraw_apply")
@Schema(name = "OwnerWithdrawApply", description = "业主提现申请表")
public class OwnerWithdrawApply implements Serializable {
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

    @Schema(description = "提现申请单号")
    @TableField("apply_no")
    private String applyNo;

    @Schema(description = "申请金额")
    @TableField("apply_amount")
    private BigDecimal applyAmount;

    @Schema(description = "手续费")
    @TableField("fee_amount")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额")
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @Schema(description = "审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回")
    @TableField("approval_status")
    private Integer approvalStatus;

    @Schema(description = "提现状态")
    @TableField("withdraw_status")
    private Integer withdrawStatus;

    @Schema(description = "收款人姓名")
    @TableField("payee_name")
    private String payeeName;

    @Schema(description = "收款账号")
    @TableField("payee_account_no")
    private String payeeAccountNo;

    @Schema(description = "开户行名称")
    @TableField("payee_bank_name")
    private String payeeBankName;

    @Schema(description = "打款渠道")
    @TableField("channel")
    private String channel;

    @Schema(description = "第三方交易号")
    @TableField("third_trade_no")
    private String thirdTradeNo;

    @Schema(description = "失败原因")
    @TableField("failure_reason")
    private String failureReason;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "申请时间")
    @TableField("applied_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appliedAt;

    @Schema(description = "审批时间")
    @TableField("approved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approvedAt;

    @Schema(description = "打款时间")
    @TableField("paid_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paidAt;

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
