package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.mapper.TenantPersonalMapper;
import com.homi.model.vo.tenant.TenantPersonalVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 租客个人信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-12-15
 */
@Service
public class TenantPersonalRepo extends ServiceImpl<TenantPersonalMapper, TenantPersonal> {
    @Cacheable(cacheNames = "tenant-personal", key = "#id")
    public TenantPersonalVO getTenantById(Long id) {
        TenantPersonal one = getOne(new LambdaQueryWrapper<TenantPersonal>().eq(TenantPersonal::getId, id));

        return BeanCopyUtils.copyBean(one, TenantPersonalVO.class);
    }
}
