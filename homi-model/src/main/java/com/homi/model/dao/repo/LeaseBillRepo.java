package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.payment.PayStatusEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.mapper.LeaseBillMapper;
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
public class LeaseBillRepo extends ServiceImpl<LeaseBillMapper, LeaseBill> {

    /**
     * 根据租约ID查询账单列表
     *
     * @param leaseId 租约ID
     * @param valid   是否有效
     * @return 账单列表
     */
    public List<LeaseBill> getBillListByLeaseId(Long leaseId, Boolean valid) {
        LambdaQueryWrapper<LeaseBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseBill::getLeaseId, leaseId);
        queryWrapper.eq(LeaseBill::getValid, valid);
        queryWrapper.orderByAsc(LeaseBill::getDueDate);
        return list(queryWrapper);
    }

    /**
     * 根据租客ID查询未支付完成的账单列表
     *
     * @param tenantId 租客ID
     * @return 未支付账单列表
     */
    public List<LeaseBill> getUnpaidBillsByTenantId(Long tenantId) {
        LambdaQueryWrapper<LeaseBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseBill::getTenantId, tenantId);
        queryWrapper.eq(LeaseBill::getValid, true);
        queryWrapper.in(LeaseBill::getPayStatus, PayStatusEnum.UNPAID.getCode(), PayStatusEnum.PARTIALLY_PAID.getCode());

        queryWrapper.orderByAsc(LeaseBill::getDueDate);

        return list(queryWrapper);
    }

    /**
     * 按租客维度查询全部押金相关账单
     */
    public List<LeaseBill> getAllDepositsByTenantId(Long tenantId) {
        return lambdaQuery()
            .eq(LeaseBill::getTenantId, tenantId)
            .in(LeaseBill::getBillType,
                com.homi.common.lib.enums.lease.LeaseBillTypeEnum.DEPOSIT.getCode(),
                com.homi.common.lib.enums.lease.LeaseBillTypeEnum.DEPOSIT_CARRY_IN.getCode(),
                com.homi.common.lib.enums.lease.LeaseBillTypeEnum.DEPOSIT_CARRY_OUT.getCode())
            .eq(LeaseBill::getValid, true)
            .orderByAsc(LeaseBill::getCreateTime)
            .list();
    }
}
