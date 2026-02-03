package com.homi.model.checkout.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退租费用明细 VO
 */
@Data
public class TenantCheckoutFeeVO {

    /**
     * 费用ID
     */
    private Long id;

    /**
     * 退租单ID
     */
    private Long checkoutId;

    /**
     * 费用类型
     */
    private Integer feeType;

    /**
     * 费用类型名称
     */
    private String feeTypeName;

    /**
     * 费用名称
     */
    private String feeName;

    /**
     * 费用金额
     */
    private BigDecimal feeAmount;

    /**
     * 方向：1=扣款，2=退款
     */
    private Integer feeDirection;

    /**
     * 方向名称
     */
    private String feeDirectionName;

    /**
     * 关联账单ID
     */
    private Long billId;

    /**
     * 备注
     */
    private String remark;
}
