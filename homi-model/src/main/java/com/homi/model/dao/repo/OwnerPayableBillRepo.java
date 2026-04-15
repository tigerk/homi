package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.mapper.OwnerPayableBillMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OwnerPayableBillRepo extends ServiceImpl<OwnerPayableBillMapper, OwnerPayableBill> {
}
