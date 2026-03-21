package com.homi.service.service.tenant;

import com.homi.model.dao.entity.Tenant;
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

    public Tenant getTenant(Long tenantId) {
        return tenantId == null ? null : tenantRepo.getById(tenantId);
    }
}
