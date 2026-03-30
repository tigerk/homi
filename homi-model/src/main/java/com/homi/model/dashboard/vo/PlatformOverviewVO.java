package com.homi.model.dashboard.vo;

import lombok.Data;

import java.util.List;

@Data
public class PlatformOverviewVO {
    private Long companyCount;

    private Long houseCount;

    private Long roomCount;

    private Long userCount;

    private List<PlatformPackageCompanyCountVO> packageCompanyCounts;

    private PlatformTrialStatsVO trialStats;
}
