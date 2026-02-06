package com.homi.model.dao.entity;

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

/**
 * <p>
 * 租客账单表
 * </p>
 *
 * @author tk
 * @since 2025-12-15
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease_bill")
@Schema(name = "LeaseBill", description = "租客账单表")
public class LeaseBill implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "租客ID")
    @TableField("tenant_id")
    private Long tenantId;

    @Schema(description = "租约ID")
    @TableField("lease_id")
    private Long leaseId;

    @Schema(description = "账单顺序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "账单类型：1=租金，2=押金，3=杂费，4=退租结算，5=押金结转入，6=押金结转出")
    @TableField("bill_type")
    private Integer billType;

    @Schema(description = "结转来源账单ID")
    @TableField("carry_over_from_bill_id")
    private Long carryOverFromBillId;

    @Schema(description = "结转目标账单ID")
    @TableField("carry_over_to_bill_id")
    private Long carryOverToBillId;

    @Schema(description = "账单租期开始日期")
    @TableField("rent_period_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date rentPeriodStart;

    @Schema(description = "账单租期结束日期")
    @TableField("rent_period_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date rentPeriodEnd;

    @Schema(description = "租金金额")
    @TableField("rental_amount")
    private BigDecimal rentalAmount;

    @Schema(description = "押金金额")
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    @Schema(description = "其他费用（如水电、物业）")
    @TableField("other_fee_amount")
    private BigDecimal otherFeeAmount;

    @Schema(description = "账单合计金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "应收日期（根据 rent_due_xxx 计算）")
    @TableField("due_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "实际支付日期")
    @TableField("pay_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "实际支付金额")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付，3=逾期")
    @TableField("pay_status")
    private Integer payStatus;

    @Schema(description = "支付方式：1=现金，2=转账，3=支付宝，4=微信，5=其他")
    @TableField("pay_channel")
    private Integer payChannel;

    @Schema(description = "备注信息")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否有效：1=有效，0=无效")
    @TableField("valid")
    private Boolean valid;

    @Schema(description = "是否删除：0=否，1=是")
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
