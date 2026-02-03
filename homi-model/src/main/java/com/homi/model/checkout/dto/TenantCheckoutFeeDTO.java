package com.homi.model.checkout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退租费用明细 DTO
 */
@Data
public class TenantCheckoutFeeDTO {

    /**
     * 费用ID（修改时传）
     */
    private Long id;

    /**
     * 费用类型
     */
    @NotNull(message = "费用类型不能为空")
    private Integer feeType;

    /**
     * 费用名称
     */
    private String feeName;

    /**
     * 费用金额
     */
    @NotNull(message = "费用金额不能为空")
    private BigDecimal feeAmount;

    /**
     * 方向：1=扣款，2=退款
     */
    @NotNull(message = "费用方向不能为空")
    private Integer feeDirection;

    /**
     * 关联账单ID
     */
    private Long billId;

    /**
     * 备注
     */
    private String remark;
}
