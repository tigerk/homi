package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerSettlementItem;
import com.homi.model.dao.mapper.OwnerSettlementItemMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerSettlementItemRepo extends ServiceImpl<OwnerSettlementItemMapper, OwnerSettlementItem> {
    public void deleteByContractIdForce(Long contractId) {
        baseMapper.deleteByContractIdForce(contractId);
    }
}
