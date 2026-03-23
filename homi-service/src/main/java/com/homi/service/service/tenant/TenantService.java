package com.homi.service.service.tenant;

import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.repo.TenantCompanyRepo;
import com.homi.model.dao.repo.TenantPersonalRepo;
import com.homi.model.dao.repo.TenantRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 租客
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepo tenantRepo;

    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;

    public Tenant getTenant(Long tenantId) {
        return tenantId == null ? null : tenantRepo.getById(tenantId);
    }

    // -------------------------------------------------------------------------
    // 私有：数据查询
    // -------------------------------------------------------------------------
    public TenantPersonal getPersonalDetail(Tenant tenant) {
        return tenant.getTenantTypeId() == null ? null : tenantPersonalRepo.getById(tenant.getTenantTypeId());
    }

    public TenantCompany getCompanyDetail(Tenant tenant) {
        return tenant.getTenantTypeId() == null ? null : tenantCompanyRepo.getById(tenant.getTenantTypeId());
    }
}
