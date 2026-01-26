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
 * 退租主表
 * </p>
 *
 * @author tk
 * @since 2026-01-26
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_checkout")
@Schema(name = "TenantCheckout", description = "退租主表")
public class TenantCheckout implements Serializable {
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

    @Schema(description = "交割单ID（关联 delivery.id，handover_type=CHECK_OUT）")
    @TableField("delivery_id")
    private Long deliveryId;

    @Schema(description = "退租类型：1=正常到期，2=提前退租，3=换房退租，4=违约退租，5=协商解约")
    @TableField("checkout_type")
    private Integer checkoutType;

    @Schema(description = "退租原因")
    @TableField("checkout_reason")
    private String checkoutReason;

    @Schema(description = "合同到期日")
    @TableField("lease_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaseEnd;

    @Schema(description = "实际退租日")
    @TableField("actual_checkout_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date actualCheckoutDate;

    @Schema(description = "押金总额")
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    @Schema(description = "扣款总额（欠租+水电+损坏+违约金等）")
    @TableField("deduction_amount")
    private BigDecimal deductionAmount;

    @Schema(description = "应退金额（多收租金+押金余额等）")
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @Schema(description = "最终结算（正数=租客补缴，负数=退还租客）")
    @TableField("final_amount")
    private BigDecimal finalAmount;

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
