package com.homi.model.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "租客支付流水汇总")
public class PaymentFlowFinanceSummaryVO {
    @Schema(description = "待审批总金额")
    private BigDecimal pendingApprovalAmount;

    @Schema(description = "今日待审批总金额")
    private BigDecimal todayPendingApprovalAmount;

    @Schema(description = "支付成功总金额")
    private BigDecimal successAmount;

    @Schema(description = "今日支付成功总金额")
    private BigDecimal todaySuccessAmount;

    @Schema(description = "已关闭总金额")
    private BigDecimal closedAmount;

    @Schema(description = "今日已关闭总金额")
    private BigDecimal todayClosedAmount;
}
