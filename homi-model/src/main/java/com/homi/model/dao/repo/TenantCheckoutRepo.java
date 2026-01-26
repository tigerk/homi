package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TenantCheckout;
import com.homi.model.dao.mapper.TenantCheckoutMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 退租主表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-01-26
 */
@Service
public class TenantCheckoutRepo extends ServiceImpl<TenantCheckoutMapper, TenantCheckout> {

    /**
     * 更新退租状态
     *
     * @param checkoutId 退租主表ID
     * @param status          状态
     */
    public void updateStatus(Long checkoutId, int status) {
        TenantCheckout tenantCheckout = new TenantCheckout();
        tenantCheckout.setId(checkoutId);
        tenantCheckout.setStatus(status);
        updateById(tenantCheckout);
    }
}
