package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.lease.LeaseContractDocStatusEnum;
import com.homi.model.dao.entity.LeaseContract;
import com.homi.model.dao.mapper.LeaseContractMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 租赁合同信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class LeaseContractRepo extends ServiceImpl<LeaseContractMapper, LeaseContract> {
    public LeaseContract getContractByLeaseId(Long leaseId) {
        return listByLeaseId(leaseId).stream()
            .filter(this::isActiveDoc)
            .findFirst()
            .orElse(null);
    }

    public List<LeaseContract> listByLeaseId(Long leaseId) {
        LambdaQueryWrapper<LeaseContract> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseContract::getLeaseId, leaseId);
        queryWrapper.orderByDesc(LeaseContract::getDocStatus);
        queryWrapper.orderByDesc(LeaseContract::getCreateAt);
        queryWrapper.orderByDesc(LeaseContract::getId);

        return list(queryWrapper);
    }

    public List<LeaseContract> listByLeaseIds(Collection<Long> leaseIds) {
        if (leaseIds == null || leaseIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<LeaseContract>()
            .in(LeaseContract::getLeaseId, leaseIds)
            .orderByDesc(LeaseContract::getDocStatus)
            .orderByDesc(LeaseContract::getCreateAt)
            .orderByDesc(LeaseContract::getId));
    }

    public boolean isActiveDoc(LeaseContract doc) {
        return doc != null && !Objects.equals(doc.getDocStatus(), LeaseContractDocStatusEnum.VOIDED.getCode());
    }

    public Integer resolveAggregateSignStatus(List<LeaseContract> docs) {
        boolean hasSignedDoc = Objects.requireNonNullElse(docs, List.<LeaseContract>of())
            .stream()
            .filter(this::isActiveDoc)
            .anyMatch(item -> Objects.equals(item.getSignStatus(), 1));
        return hasSignedDoc ? 1 : 0;
    }
}
