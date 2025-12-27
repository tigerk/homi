package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public List<TenantBill> getBillListByTenantId(Long tenantId) {
        LambdaQueryWrapper<TenantBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantBill::getTenantId, tenantId);

        queryWrapper.orderByAsc(TenantBill::getDueDate);

        return list(queryWrapper);
    }
}
