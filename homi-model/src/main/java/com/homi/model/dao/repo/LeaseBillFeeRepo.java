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

    public List<LeaseBillFee> getFeesByBillIdForUpdate(Long billId) {
        if (billId == null) {
            return List.of();
        }
        LambdaQueryWrapper<LeaseBillFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaseBillFee::getBillId, billId);
        wrapper.last("for update");
        return list(wrapper);
    }

    public List<LeaseBillFee> getFeesByBillIds(List<Long> billIds) {
        if (billIds == null || billIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<LeaseBillFee>().in(LeaseBillFee::getBillId, billIds));
    }

    public List<LeaseBillFee> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<LeaseBillFee>().in(LeaseBillFee::getId, ids));
    }

    public List<LeaseBillFee> getByIdsForUpdate(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<LeaseBillFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(LeaseBillFee::getId, ids);
        wrapper.last("for update");
        return list(wrapper);
    }

    public boolean removeByBillId(Long billId) {
        if (billId == null) {
            return false;
        }
        return remove(new LambdaQueryWrapper<LeaseBillFee>().eq(LeaseBillFee::getBillId, billId));
    }
}
