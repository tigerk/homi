package com.homi.model.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "业主账单汇总VO")
public class OwnerBillSummaryVO {
    @Schema(description = "账单数量")
    private Long billCount;

    @Schema(description = "收入总额")
    private BigDecimal totalIncomeAmount;

    @Schema(description = "应付总额")
    private BigDecimal totalPayableAmount;

    @Schema(description = "已结总额")
    private BigDecimal totalSettledAmount;

    @Schema(description = "未结总额")
    private BigDecimal totalUnpaidAmount;

    @Schema(description = "可提现总额")
    private BigDecimal totalWithdrawableAmount;
}
