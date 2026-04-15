package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerPayableBillLine;
import com.homi.model.dao.mapper.OwnerPayableBillLineMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OwnerPayableBillLineRepo extends ServiceImpl<OwnerPayableBillLineMapper, OwnerPayableBillLine> {
}
