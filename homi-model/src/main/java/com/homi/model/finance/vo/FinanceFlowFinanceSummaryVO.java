package com.homi.model.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "租客财务流水汇总")
public class FinanceFlowFinanceSummaryVO {
    @Schema(description = "待入账流水总额")
    private BigDecimal pendingAmount;

    @Schema(description = "今日待入账总额")
    private BigDecimal todayPendingAmount;

    @Schema(description = "已入账流水总额")
    private BigDecimal successAmount;

    @Schema(description = "今日已入账总额")
    private BigDecimal todaySuccessAmount;

    @Schema(description = "已作废流水总额")
    private BigDecimal voidedAmount;

    @Schema(description = "今日已作废总额")
    private BigDecimal todayVoidedAmount;
}
