package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.mapper.TenantMapper;
import com.homi.model.vo.tenant.TenantPersonalVO;
import org.springframework.cache.annotation.Cacheable;
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
    public Tenant getTenantByIdNo(String idNo) {
        return getOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getIdNo, idNo));
    }


    @Cacheable(cacheNames = "tenant-personal", key = "#id")
    public TenantPersonalVO getTenantById(Long id) {
        Tenant one = getOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getId, id));

        return BeanCopyUtils.copyBean(one, TenantPersonalVO.class);
    }
}
