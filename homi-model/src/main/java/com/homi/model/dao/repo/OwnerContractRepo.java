package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.mapper.OwnerContractMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerContractRepo extends ServiceImpl<OwnerContractMapper, OwnerContract> {
}
