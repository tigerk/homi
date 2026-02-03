package com.homi.model.dao.repo;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.checkout.CheckoutStatusEnum;
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
     * @param status     状态
     */
    public void updateStatus(Long checkoutId, int status) {
        TenantCheckout tenantCheckout = new TenantCheckout();
        tenantCheckout.setId(checkoutId);
        tenantCheckout.setStatus(status);
        tenantCheckout.setUpdateTime(DateUtil.date());
        updateById(tenantCheckout);
    }

    public void updateApprovalStatus(Long checkoutId, Integer bizApprovalStatus) {
        TenantCheckout tenantCheckout = new TenantCheckout();
        tenantCheckout.setId(checkoutId);
        tenantCheckout.setApprovalStatus(bizApprovalStatus);
        tenantCheckout.setUpdateTime(DateUtil.date());
        updateById(tenantCheckout);
    }

    public TenantCheckout getByTenantId(Long tenantId) {
        return lambdaQuery()
            .eq(TenantCheckout::getTenantId, tenantId)
            .ne(TenantCheckout::getStatus, CheckoutStatusEnum.CANCELLED.getCode())
            .last("LIMIT 1")
            .one();
    }

    public boolean hasActiveCheckout(Long tenantId) {
        return lambdaQuery()
            .eq(TenantCheckout::getTenantId, tenantId)
            .in(TenantCheckout::getStatus, CheckoutStatusEnum.DRAFT.getCode(), CheckoutStatusEnum.PENDING.getCode())
            .exists();
    }
}
