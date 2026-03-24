package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.lease.LeaseBillStatusEnum;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.mapper.LeaseBillMapper;
import com.homi.model.dashboard.vo.WelcomeOverdueBucketVO;
import com.homi.model.dashboard.vo.WelcomeOverdueTenantVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
     * @param historical   是否历史账单
     * @return 账单列表
     */
    public List<LeaseBill> getBillListByLeaseId(Long leaseId, Boolean historical) {
        LambdaQueryWrapper<LeaseBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseBill::getLeaseId, leaseId);
        if (Boolean.TRUE.equals(historical)) {
            queryWrapper.and(wrapper -> wrapper
                .eq(LeaseBill::getHistorical, true)
                .or()
                .eq(LeaseBill::getStatus, LeaseBillStatusEnum.VOIDED.getCode()));
        } else {
            queryWrapper.eq(LeaseBill::getHistorical, false);
            queryWrapper.eq(LeaseBill::getStatus, LeaseBillStatusEnum.NORMAL.getCode());
        }
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
        queryWrapper.eq(LeaseBill::getHistorical, false);
        queryWrapper.eq(LeaseBill::getStatus, LeaseBillStatusEnum.NORMAL.getCode());
        queryWrapper.in(LeaseBill::getPayStatus, PayStatusEnum.UNPAID.getCode(), PayStatusEnum.PARTIALLY_PAID.getCode());

        queryWrapper.orderByAsc(LeaseBill::getDueDate);

        return list(queryWrapper);
    }

    /**
     * 根据ID查询账单，加锁
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/23 10:18
     *
     * @param id 参数说明
     * @return com.homi.model.dao.entity.LeaseBill
     */
    public LeaseBill getByIdForUpdate(Long id) {
        if (id == null) {
            return null;
        }
        LambdaQueryWrapper<LeaseBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseBill::getId, id);
        queryWrapper.last("for update");
        return getOne(queryWrapper);
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
            .eq(LeaseBill::getHistorical, false)
            .eq(LeaseBill::getStatus, LeaseBillStatusEnum.NORMAL.getCode())
            .orderByAsc(LeaseBill::getCreateTime)
            .list();
    }

    public List<WelcomeOverdueBucketVO> getWelcomeOverdueBuckets() {
        return getBaseMapper().selectWelcomeOverdueBuckets();
    }

    public BigDecimal getNext7DaysReceivableAmount() {
        return getBaseMapper().selectNext7DaysReceivableAmount();
    }

    public List<WelcomeOverdueTenantVO> getWelcomeOverdueTenantTopList() {
        return getBaseMapper().selectWelcomeOverdueTenantTopList();
    }
}
