package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.mapper.LeaseBillFeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 租客账单费用明细表 服务实现类
 * </p>
 */
@Service
public class LeaseBillFeeRepo extends ServiceImpl<LeaseBillFeeMapper, LeaseBillFee> {

    public List<LeaseBillFee> getFeesByBillId(Long billId) {
        return list(new LambdaQueryWrapper<LeaseBillFee>().eq(LeaseBillFee::getBillId, billId));
    }

    public List<LeaseBillFee> getFeesByBillIds(List<Long> billIds) {
        if (billIds == null || billIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<LeaseBillFee>().in(LeaseBillFee::getBillId, billIds));
    }

    public boolean removeByBillId(Long billId) {
        if (billId == null) {
            return false;
        }
        return remove(new LambdaQueryWrapper<LeaseBillFee>().eq(LeaseBillFee::getBillId, billId));
    }
}
