package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "欢迎页时间维度金额统计")
public class WelcomePeriodAmountVO {
    @Schema(description = "今日总额")
    private BigDecimal todayAmount;

    @Schema(description = "昨日总额")
    private BigDecimal yesterdayAmount;

    @Schema(description = "本月总额")
    private BigDecimal thisMonthAmount;

    @Schema(description = "上月总额")
    private BigDecimal lastMonthAmount;

    @Schema(description = "本年总额")
    private BigDecimal thisYearAmount;

    @Schema(description = "累计总额")
    private BigDecimal totalAmount;
}
