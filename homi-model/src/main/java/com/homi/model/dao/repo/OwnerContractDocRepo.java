package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.owner.OwnerSignStatusEnum;
import com.homi.model.dao.entity.OwnerContractDoc;
import com.homi.model.dao.mapper.OwnerContractDocMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class OwnerContractDocRepo extends ServiceImpl<OwnerContractDocMapper, OwnerContractDoc> {

    public List<OwnerContractDoc> listByOwnerContractId(Long ownerContractId) {
        return list(new LambdaQueryWrapper<OwnerContractDoc>()
            .eq(OwnerContractDoc::getOwnerContractId, ownerContractId)
            .orderByDesc(OwnerContractDoc::getCreateAt)
            .orderByDesc(OwnerContractDoc::getId));
    }

    public List<OwnerContractDoc> listByOwnerContractIds(Collection<Long> ownerContractIds) {
        if (ownerContractIds == null || ownerContractIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<OwnerContractDoc>()
            .in(OwnerContractDoc::getOwnerContractId, ownerContractIds)
            .orderByDesc(OwnerContractDoc::getCreateAt)
            .orderByDesc(OwnerContractDoc::getId));
    }

    public boolean existsSignedDoc(Long ownerContractId) {
        return listByOwnerContractId(ownerContractId)
            .stream()
            .anyMatch(item -> Objects.equals(item.getSignStatus(), OwnerSignStatusEnum.SIGNED.getCode()));
    }

    public Integer resolveAggregateSignStatus(Long ownerContractId) {
        return resolveAggregateSignStatus(listByOwnerContractId(ownerContractId));
    }

    public Integer resolveAggregateSignStatus(List<OwnerContractDoc> docs) {
        boolean hasSignedDoc = Objects.requireNonNullElse(docs, List.<OwnerContractDoc>of())
            .stream()
            .anyMatch(item -> Objects.equals(item.getSignStatus(), OwnerSignStatusEnum.SIGNED.getCode()));
        return hasSignedDoc ? OwnerSignStatusEnum.SIGNED.getCode() : OwnerSignStatusEnum.PENDING.getCode();
    }
}
