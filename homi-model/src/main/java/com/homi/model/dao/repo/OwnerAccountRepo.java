package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerAccount;
import com.homi.model.dao.mapper.OwnerAccountMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerAccountRepo extends ServiceImpl<OwnerAccountMapper, OwnerAccount> {
    public OwnerAccount getByOwnerId(Long ownerId) {
        return getOne(new LambdaQueryWrapper<OwnerAccount>().eq(OwnerAccount::getOwnerId, ownerId));
    }
}
