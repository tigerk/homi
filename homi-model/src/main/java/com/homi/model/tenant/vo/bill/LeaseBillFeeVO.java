package com.homi.model.tenant.vo.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "租客账单费用明细VO")
public class LeaseBillFeeVO implements Serializable {
    @Schema(description = "费用项ID")
    private Long id;

    @Schema(description = "账单ID（关联 lease_bill.id）")
    private Long billId;

    @Schema(description = "费用类型：RENTAL/DEPOSIT/OTHER_FEE")
    private String feeType;

    @Schema(description = "费用字典 ID")
    private Long dictDataId;

    @Schema(description = "费用名称")
    private String feeName;

    @Schema(description = "费用金额")
    private BigDecimal amount;

    @Schema(description = "已收金额")
    private BigDecimal paidAmount;

    @Schema(description = "待收金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付")
    private Integer payStatus;

    @Schema(description = "费用周期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeStartDate;

    @Schema(description = "费用周期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeEndDate;

    @Schema(description = "备注")
    private String remark;
}
