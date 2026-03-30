package com.homi.model.dashboard.vo;

import lombok.Data;

@Data
public class PlatformTrialStatsVO {
    private Long totalCount;

    private Long pendingCount;

    private Long approvedCount;

    private Long rejectedCount;
}
