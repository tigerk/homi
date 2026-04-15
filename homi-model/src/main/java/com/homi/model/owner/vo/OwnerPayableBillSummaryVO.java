package com.homi.model.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "包租业主应付单汇总VO")
public class OwnerPayableBillSummaryVO {
    @Schema(description = "应付单数量")
    private Long billCount;

    @Schema(description = "应付总额")
    private BigDecimal totalPayableAmount;

    @Schema(description = "已付总额")
    private BigDecimal totalPaidAmount;

    @Schema(description = "未付总额")
    private BigDecimal totalUnpaidAmount;

    @Schema(description = "作废数量")
    private Long canceledCount;
}
