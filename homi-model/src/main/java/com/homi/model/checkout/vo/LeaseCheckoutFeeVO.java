package com.homi.model.checkout.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.FeeDirectionEnum;
import com.homi.common.lib.enums.lease.LeaseBillTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * 收支类型
     */
    @Schema(description = "收支类型", implementation = FeeDirectionEnum.class)
    private String feeDirection;

    /**
     * 收支类型名称
     */
    private String feeDirectionName;

    /**
     * 费用类型
     */
    @Schema(description = "费用类型", implementation = LeaseBillTypeEnum.class)
    private Integer feeType;

    /**
     * 费用类型名称
     */
    private String feeTypeName;

    /**
     * 费用字典数据项ID
     */
    private Long dictDataId;

    /**
     * 费用名称快照（如"房屋押金"）
     */
    private String feeName;

    /**
     * 费用金额
     */
    private BigDecimal feeAmount;

    /**
     * 费用周期开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeStartDate;

    /**
     * 费用周期结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeEndDate;

    /**
     * 费用备注
     */
    private String remark;

    /**
     * 关联租客账单ID
     */
    private Long leaseBillId;
}
