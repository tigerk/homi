package com.homi.model.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.mapper.TenantMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    /**
     * 获取租客列表
     *
     * @param name       租客名称
     * @param phone      租客手机号
     * @param tenantType 租客类型
     * @return 租客列表
     */
    public List<Tenant> getTenantList(String name, String phone, Integer tenantType) {
        LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(name)) {
            query.like(Tenant::getTenantName, name);
        }
        if (CharSequenceUtil.isNotBlank(phone)) {
            query.like(Tenant::getTenantPhone, phone);
        }

        if (Objects.nonNull(tenantType)) {
            query.eq(Tenant::getTenantType, tenantType);
        }

        return list(query);
    }
}
