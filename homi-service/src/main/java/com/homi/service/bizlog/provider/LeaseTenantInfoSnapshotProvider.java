package com.homi.service.bizlog.provider;

import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.dao.repo.TenantCompanyRepo;
import com.homi.model.dao.repo.TenantPersonalRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.service.bizlog.BizOperateLogSnapshotProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("leaseTenantInfoSnapshotProvider")
@RequiredArgsConstructor
public class LeaseTenantInfoSnapshotProvider implements BizOperateLogSnapshotProvider {
    private final LeaseRepo leaseRepo;
    private final TenantRepo tenantRepo;
    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;

    @Override
    public Object getBeforeSnapshot(Object[] args) {
        return buildSnapshot(resolveLeaseId(args));
    }

    @Override
    public Object getAfterSnapshot(Object[] args, Object result) {
        return buildSnapshot(resolveLeaseId(args));
    }

    private Long resolveLeaseId(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Object value = arg.getClass().getMethod("getLeaseId").invoke(arg);
                return value instanceof Long id ? id : null;
            } catch (Exception _) {
                // Continue scanning other arguments.
            }
        }
        return null;
    }

    private LeaseTenantInfoLogSnapshot buildSnapshot(Long leaseId) {
        if (leaseId == null) {
            return null;
        }
        Lease lease = leaseRepo.getById(leaseId);
        if (lease == null) {
            return null;
        }
        Tenant tenant = lease.getTenantId() == null ? null : tenantRepo.getById(lease.getTenantId());
        TenantPersonal personal = null;
        TenantCompany company = null;
        if (tenant != null && tenant.getTenantTypeId() != null) {
            if (Integer.valueOf(0).equals(tenant.getTenantType())) {
                personal = tenantPersonalRepo.getById(tenant.getTenantTypeId());
            } else {
                company = tenantCompanyRepo.getById(tenant.getTenantTypeId());
            }
        }
        return new LeaseTenantInfoLogSnapshot(lease, tenant, personal, company);
    }

    @Data
    @AllArgsConstructor
    public static class LeaseTenantInfoLogSnapshot {
        private Lease lease;
        private Tenant tenant;
        private TenantPersonal tenantPersonal;
        private TenantCompany tenantCompany;
    }
}
