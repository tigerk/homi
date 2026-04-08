package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerContractSubject;
import com.homi.model.dao.mapper.OwnerContractSubjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerContractSubjectRepo extends ServiceImpl<OwnerContractSubjectMapper, OwnerContractSubject> {
    public List<OwnerContractSubject> listByContractId(Long contractId) {
        return list(new LambdaQueryWrapper<OwnerContractSubject>().eq(OwnerContractSubject::getContractId, contractId));
    }

    public void deleteByContractIdForce(Long contractId) {
        baseMapper.deleteByContractIdForce(contractId);
    }
}
