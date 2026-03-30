package com.homi.model.dashboard.vo;

import lombok.Data;

import java.util.List;

@Data
public class PlatformOverviewVO {
    private Long companyCount;

    private List<PlatformPackageCompanyCountVO> packageCompanyCounts;

    private PlatformTrialStatsVO trialStats;
}
