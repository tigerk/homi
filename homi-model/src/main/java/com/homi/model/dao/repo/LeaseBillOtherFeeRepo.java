package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.LeaseBillOtherFee;
import com.homi.model.dao.mapper.LeaseBillOtherFeeMapper;
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
public class LeaseBillOtherFeeRepo extends ServiceImpl<LeaseBillOtherFeeMapper, LeaseBillOtherFee> {

    /**
     * 根据账单ID查询其他费用
     *
     * @param billId 账单ID
     * @return 其他费用列表
     */
    public List<LeaseBillOtherFee> getOtherFeesByBillId(Long billId) {
        return list(new LambdaQueryWrapper<LeaseBillOtherFee>().eq(LeaseBillOtherFee::getBillId, billId));
    }

    /**
     * 根据账单ID列表批量查询其他费用
     *
     * @param billIds 账单ID列表
     * @return 其他费用列表
     */
    public List<LeaseBillOtherFee> getOtherFeesByBillIds(List<Long> billIds) {
        if (billIds == null || billIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<LeaseBillOtherFee>().in(LeaseBillOtherFee::getBillId, billIds));
    }
}
