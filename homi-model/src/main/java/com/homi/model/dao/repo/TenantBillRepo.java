package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.payment.PayStatusEnum;
import com.homi.model.dao.entity.TenantBill;
import com.homi.model.dao.mapper.TenantBillMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 租客账单表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class TenantBillRepo extends ServiceImpl<TenantBillMapper, TenantBill> {

    /**
     * 根据租客ID查询账单列表
     *
     * @param tenantId 租客ID
     * @return 账单列表
     */
    public List<TenantBill> getBillListByTenantId(Long tenantId, Boolean valid) {
        LambdaQueryWrapper<TenantBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantBill::getTenantId, tenantId);
        queryWrapper.eq(TenantBill::getValid, valid);

        queryWrapper.orderByAsc(TenantBill::getDueDate);

        return list(queryWrapper);
    }

    /**
     * 根据租客ID查询未支付完成的账单列表
     *
     * @param tenantId 租客ID
     * @return 未支付账单列表
     */
    public List<TenantBill> getUnpaidBillsByTenantId(Long tenantId) {
        LambdaQueryWrapper<TenantBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantBill::getTenantId, tenantId);
        queryWrapper.eq(TenantBill::getValid, true);
        queryWrapper.in(TenantBill::getPayStatus, PayStatusEnum.UNPAID.getCode(), PayStatusEnum.PARTIALLY_PAID.getCode());

        queryWrapper.orderByAsc(TenantBill::getDueDate);

        return list(queryWrapper);
    }
}
