package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.mapper.TenantMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 租客信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class TenantRepo extends ServiceImpl<TenantMapper, Tenant> {
    public Tenant findOrCreateTenant(Long companyId, Integer tenantType, Long tenantTypeId,
                                     String tenantName, String tenantPhone, Long createBy) {
        Tenant existing = lambdaQuery()
            .eq(Tenant::getTenantTypeId, tenantTypeId)
            .eq(Tenant::getTenantType, tenantType)
            .one();

        if (existing != null) {
            return existing;
        }

        Tenant tenant = new Tenant();
        tenant.setCompanyId(companyId);
        tenant.setTenantType(tenantType);
        tenant.setTenantTypeId(tenantTypeId);
        tenant.setTenantName(tenantName);
        tenant.setTenantPhone(tenantPhone);
        tenant.setStatus(1);
        tenant.setCreateBy(createBy);
        tenant.setCreateTime(new java.util.Date());
        save(tenant);
        return tenant;
    }
}
