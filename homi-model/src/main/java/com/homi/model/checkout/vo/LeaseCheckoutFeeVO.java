package com.homi.model.checkout.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退租费用明细 VO
 * 对应截图中费用清算表格行的展示
 */
@Data
public class LeaseCheckoutFeeVO {

    /**
     * 费用ID
     */
    private Long id;

    /**
     * 退租单ID
     */
    private Long checkoutId;

    /**
     * 收支类型：1=收，2=支
     */
    private Integer feeDirection;

    /**
     * 收支类型名称
     */
    private String feeDirectionName;

    /**
     * 费用类型
     */
    private Integer feeType;

    /**
     * 费用类型名称
     */
    private String feeTypeName;

    /**
     * 费用子类名称（如"房屋押金"）
     */
    private String feeSubName;

    /**
     * 费用金额
     */
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
     * 关联账单ID
     */
    private Long billId;
}
