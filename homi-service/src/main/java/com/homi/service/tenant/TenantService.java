package com.homi.service.tenant;

import com.homi.domain.base.PageVO;
import com.homi.domain.dto.tenant.TenantQueryDTO;
import com.homi.domain.vo.tenant.TenantItemVO;
import com.homi.model.repo.TenantRepo;
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

    public PageVO<TenantItemVO> getTenantList(TenantQueryDTO query) {
        return tenantRepo.queryTenantList(query);
    }
}
