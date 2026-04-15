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
 * 轻托管结算规则表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_settlement_rule")
@Schema(name = "OwnerSettlementRule", description = "轻托管结算规则表")
public class OwnerSettlementRule implements Serializable {
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

    @Schema(description = "业主合同房源ID")
    @TableField("contract_subject_id")
    private Long contractSubjectId;

    @Schema(description = "规则版本")
    @TableField("rule_version")
    private Integer ruleVersion;

    @Schema(description = "收入口径：RECEIVED/RECEIVABLE")
    @TableField("income_basis")
    private String incomeBasis;

    @Schema(description = "结算模式：FIXED/SHARE_GROSS/SHARE_NET/GUARANTEE_PLUS_SHARE/AGENCY")
    @TableField("settlement_mode")
    private String settlementMode;

    @Schema(description = "保底租金")
    @TableField("guaranteed_rent_amount")
    private BigDecimal guaranteedRentAmount;

    @Schema(description = "是否有保底租金")
    @TableField("has_guaranteed_rent")
    private Boolean hasGuaranteedRent;

    @Schema(description = "佣金方式：RATIO/FIXED")
    @TableField("commission_mode")
    private String commissionMode;

    @Schema(description = "佣金值")
    @TableField("commission_value")
    private BigDecimal commissionValue;

    @Schema(description = "服务费方式：RATIO/FIXED")
    @TableField("service_fee_mode")
    private String serviceFeeMode;

    @Schema(description = "服务费值")
    @TableField("service_fee_value")
    private BigDecimal serviceFeeValue;

    @Schema(description = "是否启用管理费")
    @TableField("management_fee_enabled")
    private Boolean managementFeeEnabled;

    @Schema(description = "管理费方式")
    @TableField("management_fee_mode")
    private String managementFeeMode;

    @Schema(description = "管理费值")
    @TableField("management_fee_value")
    private BigDecimal managementFeeValue;

    @Schema(description = "税费承担方：PLATFORM/OWNER/SHARED")
    @TableField("bear_tax_type")
    private String bearTaxType;

    @Schema(description = "支付手续费承担方式")
    @TableField("payment_fee_bear_type")
    private String paymentFeeBearType;

    @Schema(description = "分账时间")
    @TableField("settlement_timing")
    private String settlementTiming;

    @Schema(description = "是否启用免租规则")
    @TableField("rent_free_enabled")
    private Boolean rentFreeEnabled;

    @Schema(description = "计算优先级")
    @TableField("calc_priority")
    private Integer calcPriority;

    @Schema(description = "生效开始日期")
    @TableField("effective_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveStart;

    @Schema(description = "生效结束日期")
    @TableField("effective_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveEnd;

    @Schema(description = "状态：1=启用，0=禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "规则快照")
    @TableField("rule_snapshot")
    private String ruleSnapshot;

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
