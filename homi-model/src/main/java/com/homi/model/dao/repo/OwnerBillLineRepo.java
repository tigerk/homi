package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerBillLine;
import com.homi.model.dao.mapper.OwnerBillLineMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerBillLineRepo extends ServiceImpl<OwnerBillLineMapper, OwnerBillLine> {
}
