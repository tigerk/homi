package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerLeaseFee;
import com.homi.model.dao.mapper.OwnerLeaseFeeMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerLeaseFeeRepo extends ServiceImpl<OwnerLeaseFeeMapper, OwnerLeaseFee> {
    public void deleteByContractIdForce(Long contractId) {
        baseMapper.deleteByContractIdForce(contractId);
    }
}
