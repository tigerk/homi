package com.homi.model.dao.repo;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.checkout.CheckoutStatusEnum;
import com.homi.model.dao.entity.LeaseCheckout;
import com.homi.model.dao.mapper.LeaseCheckoutMapper;
import org.springframework.stereotype.Service;

/**
 * 退租主表 服务实现类
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class LeaseCheckoutRepo extends ServiceImpl<LeaseCheckoutMapper, LeaseCheckout> {

    /**
     * 更新退租状态
     */
    public void updateStatus(Long checkoutId, int status) {
        LeaseCheckout tenantCheckout = new LeaseCheckout();
        tenantCheckout.setId(checkoutId);
        tenantCheckout.setStatus(status);
        tenantCheckout.setUpdateTime(DateUtil.date());
        updateById(tenantCheckout);
    }

    /**
     * 更新审批状态
     */
    public void updateApprovalStatus(Long checkoutId, Integer bizApprovalStatus) {
        LeaseCheckout tenantCheckout = new LeaseCheckout();
        tenantCheckout.setId(checkoutId);
        tenantCheckout.setApprovalStatus(bizApprovalStatus);
        tenantCheckout.setUpdateTime(DateUtil.date());
        updateById(tenantCheckout);
    }

    /**
     * 根据租客ID获取非取消状态的退租单
     */
    public LeaseCheckout getByTenantId(Long tenantId) {
        return lambdaQuery()
            .eq(LeaseCheckout::getTenantId, tenantId)
            .ne(LeaseCheckout::getStatus, CheckoutStatusEnum.CANCELLED.getCode())
            .last("LIMIT 1")
            .one();
    }

    /**
     * 根据租约ID获取非取消状态的退租单
     */
    public LeaseCheckout getByLeaseId(Long leaseId) {
        return lambdaQuery()
            .eq(LeaseCheckout::getLeaseId, leaseId)
            .ne(LeaseCheckout::getStatus, CheckoutStatusEnum.CANCELLED.getCode())
            .last("LIMIT 1")
            .one();
    }

    /**
     * 判断租客是否有进行中的退租单（草稿或待确认）
     */
    public boolean hasActiveCheckout(Long tenantId) {
        return lambdaQuery()
            .eq(LeaseCheckout::getTenantId, tenantId)
            .in(LeaseCheckout::getStatus, CheckoutStatusEnum.DRAFT.getCode(), CheckoutStatusEnum.PENDING.getCode())
            .exists();
    }

    /**
     * 判断租约是否有进行中的退租单（草稿或待确认）
     */
    public boolean hasActiveCheckoutByLeaseId(Long leaseId) {
        return lambdaQuery()
            .eq(LeaseCheckout::getLeaseId, leaseId)
            .in(LeaseCheckout::getStatus, CheckoutStatusEnum.DRAFT.getCode(), CheckoutStatusEnum.PENDING.getCode())
            .exists();
    }
}
