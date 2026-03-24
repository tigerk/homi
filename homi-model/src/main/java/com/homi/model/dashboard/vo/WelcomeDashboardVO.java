package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "欢迎页聚合数据")
public class WelcomeDashboardVO {
    @Schema(description = "财务流水金额统计")
    private WelcomePeriodAmountVO financeSummary;

    @Schema(description = "支付金额统计")
    private WelcomePeriodAmountVO paymentSummary;

    @Schema(description = "最新公告")
    private List<WelcomeNoticeVO> notices;

    @Schema(description = "房源概况")
    private List<WelcomeRoomOverviewVO> roomOverviewList;

    @Schema(description = "租客逾期欠款分桶")
    private List<WelcomeOverdueBucketVO> overdueBuckets;

    @Schema(description = "空置时长分布")
    private List<WelcomeCountBucketVO> vacancyBuckets;

    @Schema(description = "合同与应收预警")
    private WelcomeContractWarningVO contractWarning;

    @Schema(description = "逾期租客Top5")
    private List<WelcomeOverdueTenantVO> overdueTenantTopList;

    @Schema(description = "租客统计")
    private WelcomeTenantStatsVO tenantStats;
}
