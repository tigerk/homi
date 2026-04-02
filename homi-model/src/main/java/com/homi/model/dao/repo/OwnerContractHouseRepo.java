package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerContractHouse;
import com.homi.model.dao.mapper.OwnerContractHouseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerContractHouseRepo extends ServiceImpl<OwnerContractHouseMapper, OwnerContractHouse> {
    public List<OwnerContractHouse> listByContractId(Long contractId) {
        return list(new LambdaQueryWrapper<OwnerContractHouse>().eq(OwnerContractHouse::getContractId, contractId));
    }
}
