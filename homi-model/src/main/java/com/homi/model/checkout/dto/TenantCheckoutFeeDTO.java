package com.homi.model.checkout.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退租费用明细 DTO
 * 对应截图中的费用清算表格行
 */
@Data
public class TenantCheckoutFeeDTO {

    /**
     * 费用ID（修改时传）
     */
    private Long id;

    /**
     * 收支类型：1=收（租客应付），2=支（退还租客）
     */
    @NotNull(message = "收支类型不能为空")
    private Integer feeDirection;

    /**
     * 费用类型（枚举code）
     */
    @NotNull(message = "费用类型不能为空")
    private Integer feeType;

    /**
     * 费用子类名称（如"房屋押金"）
     */
    private String feeSubName;

    /**
     * 费用金额
     */
    @NotNull(message = "费用金额不能为空")
    private BigDecimal feeAmount;

    /**
     * 费用周期开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feePeriodStart;

    /**
     * 费用周期结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feePeriodEnd;

    /**
     * 费用备注
     */
    private String remark;

    /**
     * 关联账单ID（如有）
     */
    private Long billId;
}
