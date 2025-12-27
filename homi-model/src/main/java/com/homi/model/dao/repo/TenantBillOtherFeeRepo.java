package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TenantBillOtherFee;
import com.homi.model.dao.mapper.TenantBillOtherFeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 租客账单其他费用明细表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-12-26
 */
@Service
public class TenantBillOtherFeeRepo extends ServiceImpl<TenantBillOtherFeeMapper, TenantBillOtherFee> {

    /**
     * 根据账单ID查询其他费用
     *
     * @param billId 账单ID
     * @return 其他费用列表
     */
    public List<TenantBillOtherFee> getOtherFeesByBillId(Long billId) {
        return list(new LambdaQueryWrapper<TenantBillOtherFee>().eq(TenantBillOtherFee::getBillId, billId));
    }
}
