package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerRentFreeRule;
import com.homi.model.dao.mapper.OwnerRentFreeRuleMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerRentFreeRuleRepo extends ServiceImpl<OwnerRentFreeRuleMapper, OwnerRentFreeRule> {
    public void deleteByContractIdForce(Long contractId) {
        baseMapper.deleteByContractIdForce(contractId);
    }
}
