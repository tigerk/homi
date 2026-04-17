package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerSettlementFee;
import com.homi.model.dao.mapper.OwnerSettlementFeeMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerSettlementFeeRepo extends ServiceImpl<OwnerSettlementFeeMapper, OwnerSettlementFee> {
    public void deleteByContractIdForce(Long contractId) {
        baseMapper.deleteByContractIdForce(contractId);
    }
}
