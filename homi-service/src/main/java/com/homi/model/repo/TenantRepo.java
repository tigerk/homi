package com.homi.model.repo;

import com.homi.domain.base.PageVO;
import com.homi.domain.dto.tenant.TenantQueryDTO;
import com.homi.domain.vo.tenant.TenantItemVO;
import com.homi.model.entity.Tenant;
import com.homi.model.mapper.TenantMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    public PageVO<TenantItemVO> queryTenantList(TenantQueryDTO query) {
        return null;
    }
}
