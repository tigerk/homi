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
 * 包租规则表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_lease_rule")
@Schema(name = "OwnerLeaseRule", description = "包租规则表")
public class OwnerLeaseRule implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业主合同ID")
    @TableField("contract_id")
    private Long contractId;

    @Schema(description = "总月租金")
    @TableField("rent_amount")
    private BigDecimal rentAmount;

    @Schema(description = "总押金")
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    @Schema(description = "押金月数")
    @TableField("deposit_months")
    private Integer depositMonths;

    @Schema(description = "付款月数")
    @TableField("payment_months")
    private Integer paymentMonths;

    @Schema(description = "付款方式文案")
    @TableField("pay_way")
    private String payWay;

    @Schema(description = "收租类型：1=提前，2=固定，3=延后")
    @TableField("rent_due_type")
    private Integer rentDueType;

    @Schema(description = "固定收租日")
    @TableField("rent_due_day")
    private Integer rentDueDay;

    @Schema(description = "收租偏移天数")
    @TableField("rent_due_offset_days")
    private Integer rentDueOffsetDays;

    @Schema(description = "首付日期")
    @TableField("first_pay_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date firstPayDate;

    @Schema(description = "交房日期")
    @TableField("handover_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handoverDate;

    @Schema(description = "承租用途")
    @TableField("usage_type")
    private String usageType;

    @Schema(description = "计费开始日期")
    @TableField("billing_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date billingStart;

    @Schema(description = "计费结束日期")
    @TableField("billing_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date billingEnd;

    @Schema(description = "折算方式：BY_DAYS/FULL_PERIOD")
    @TableField("prorate_type")
    private String prorateType;

    @Schema(description = "状态：1=启用，0=禁用")
    @TableField("status")
    private Integer status;

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
