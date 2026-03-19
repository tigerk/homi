package com.homi.model.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "租客账单财务页汇总")
public class LeaseBillFinanceSummaryVO {
    @Schema(description = "应收")
    private BigDecimal receivableAmount;

    @Schema(description = "本日应收")
    private BigDecimal todayReceivableAmount;

    @Schema(description = "已付")
    private BigDecimal paidAmount;

    @Schema(description = "今日已付")
    private BigDecimal todayPaidAmount;

    @Schema(description = "分类汇总")
    private List<CategoryStatVO> categoryStats;

    @Data
    @Schema(description = "费用分类汇总")
    public static class CategoryStatVO {
        @Schema(description = "费用类型")
        private String feeType;

        @Schema(description = "费用类型名称")
        private String feeTypeLabel;

        @Schema(description = "应收")
        private BigDecimal receivableAmount;

        @Schema(description = "已付")
        private BigDecimal paidAmount;

        @Schema(description = "待收")
        private BigDecimal unpaidAmount;
    }
}
