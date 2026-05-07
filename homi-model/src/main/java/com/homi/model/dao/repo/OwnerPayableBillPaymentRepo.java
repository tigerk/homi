package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerPayableBillPayment;
import com.homi.model.dao.mapper.OwnerPayableBillPaymentMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OwnerPayableBillPaymentRepo extends ServiceImpl<OwnerPayableBillPaymentMapper, OwnerPayableBillPayment> {
    public OwnerPayableBillPayment getByIdForUpdate(Long id) {
        if (id == null) {
            return null;
        }
        return lambdaQuery()
            .eq(OwnerPayableBillPayment::getId, id)
            .last("for update")
            .one();
    }
}
