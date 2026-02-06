package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.LeaseContract;
import com.homi.model.dao.mapper.LeaseContractMapper;
import org.springframework.stereotype.Service;

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
        LambdaQueryWrapper<LeaseContract> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseContract::getLeaseId, leaseId);

        return getOne(queryWrapper);
    }
}
