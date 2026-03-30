package com.homi.service.service.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.TrialApplicationStatusEnum;
import com.homi.model.dao.entity.Company;
import com.homi.model.dao.entity.TrialApplication;
import com.homi.model.dao.repo.CompanyPackageRepo;
import com.homi.model.dao.repo.CompanyRepo;
import com.homi.model.dao.repo.TrialApplicationRepo;
import com.homi.model.dashboard.vo.PlatformOverviewVO;
import com.homi.model.dashboard.vo.PlatformPackageCompanyCountVO;
import com.homi.model.dashboard.vo.PlatformTrialStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformDashboardService {
    private final CompanyRepo companyRepo;
    private final CompanyPackageRepo companyPackageRepo;
    private final TrialApplicationRepo trialApplicationRepo;

    public PlatformOverviewVO getOverview() {
        PlatformOverviewVO overview = new PlatformOverviewVO();
        overview.setCompanyCount(companyRepo.count());
        overview.setPackageCompanyCounts(buildPackageCompanyCounts());
        overview.setTrialStats(buildTrialStats());
        return overview;
    }

    private List<PlatformPackageCompanyCountVO> buildPackageCompanyCounts() {
        Map<Long, Integer> companyCountByPackageId = companyRepo.list().stream()
            .filter(company -> Objects.nonNull(company.getPackageId()))
            .collect(Collectors.groupingBy(Company::getPackageId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        return companyPackageRepo.list().stream()
            .map(companyPackage -> {
                PlatformPackageCompanyCountVO vo = new PlatformPackageCompanyCountVO();
                vo.setPackageId(companyPackage.getId());
                vo.setPackageName(companyPackage.getName());
                vo.setCompanyCount(companyCountByPackageId.getOrDefault(companyPackage.getId(), 0));
                return vo;
            })
            .toList();
    }

    private PlatformTrialStatsVO buildTrialStats() {
        PlatformTrialStatsVO vo = new PlatformTrialStatsVO();
        vo.setTotalCount(trialApplicationRepo.count());
        vo.setPendingCount(trialApplicationRepo.count(new LambdaQueryWrapper<TrialApplication>()
            .eq(TrialApplication::getStatus, TrialApplicationStatusEnum.PENDING.getCode())));
        vo.setApprovedCount(trialApplicationRepo.count(new LambdaQueryWrapper<TrialApplication>()
            .eq(TrialApplication::getStatus, TrialApplicationStatusEnum.APPROVED.getCode())));
        vo.setRejectedCount(trialApplicationRepo.count(new LambdaQueryWrapper<TrialApplication>()
            .eq(TrialApplication::getStatus, TrialApplicationStatusEnum.REJECTED.getCode())));
        return vo;
    }
}
