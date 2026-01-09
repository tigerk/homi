package com.homi.model.dao.repo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.mapper.TenantPersonalMapper;
import com.homi.model.tenant.vo.TenantPersonalVO;
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

        TenantPersonalVO tenantPersonalVO = BeanCopyUtils.copyBean(one, TenantPersonalVO.class);

        assert tenantPersonalVO != null;
        tenantPersonalVO.setTags(JSONUtil.toList(one.getTags(), String.class));

        return tenantPersonalVO;
    }
}
