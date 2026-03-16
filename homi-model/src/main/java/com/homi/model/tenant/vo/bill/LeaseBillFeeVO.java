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
    @Schema(description = "账单ID（关联 lease_bill.id）")
    private Long billId;

    @Schema(description = "费用类型：RENTAL/DEPOSIT/OTHER_FEE")
    private String feeType;

    @Schema(description = "费用字典 ID")
    private Long dictDataId;

    @Schema(description = "费用名称")
    private String name;

    @Schema(description = "费用金额")
    private BigDecimal amount;

    @Schema(description = "费用周期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date feeStart;

    @Schema(description = "费用周期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date feeEnd;

    @Schema(description = "备注")
    private String remark;
}
