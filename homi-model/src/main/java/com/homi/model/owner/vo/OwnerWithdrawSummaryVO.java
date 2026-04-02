package com.homi.model.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "业主提现汇总VO")
public class OwnerWithdrawSummaryVO {
    @Schema(description = "提现申请数量")
    private Long applyCount;

    @Schema(description = "待审批数量")
    private Long pendingApprovalCount;

    @Schema(description = "打款中数量")
    private Long processingCount;

    @Schema(description = "打款成功数量")
    private Long successCount;

    @Schema(description = "申请总金额")
    private BigDecimal totalApplyAmount;

    @Schema(description = "实际到账总金额")
    private BigDecimal totalActualAmount;

    @Schema(description = "账户可用余额")
    private BigDecimal availableAmount;

    @Schema(description = "账户冻结金额")
    private BigDecimal frozenAmount;
}
