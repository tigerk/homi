package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "欢迎页租客统计")
public class WelcomeTenantStatsVO {
    @Schema(description = "今日定金租客数量")
    private Integer todayDepositCount;

    @Schema(description = "本月定金租客数量")
    private Integer monthDepositCount;

    @Schema(description = "今日新签数量")
    private Integer todayNewSignCount;

    @Schema(description = "本月新签数量")
    private Integer monthNewSignCount;

    @Schema(description = "今日续签数量")
    private Integer todayRenewCount;

    @Schema(description = "本月续签数量")
    private Integer monthRenewCount;
}
