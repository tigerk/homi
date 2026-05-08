package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerPayableBillFee;
import com.homi.model.dao.mapper.OwnerPayableBillFeeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OwnerPayableBillFeeRepo extends ServiceImpl<OwnerPayableBillFeeMapper, OwnerPayableBillFee> {
    public List<OwnerPayableBillFee> getByBillIdForUpdate(Long billId) {
        if (billId == null) {
            return List.of();
        }
        LambdaQueryWrapper<OwnerPayableBillFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OwnerPayableBillFee::getBillId, billId);
        wrapper.orderByAsc(OwnerPayableBillFee::getId);
        wrapper.last("for update");
        return list(wrapper);
    }

    public List<OwnerPayableBillFee> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OwnerPayableBillFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OwnerPayableBillFee::getId, ids);
        return list(wrapper);
    }
}
