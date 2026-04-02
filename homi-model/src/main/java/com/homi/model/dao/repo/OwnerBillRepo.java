package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerBill;
import com.homi.model.dao.mapper.OwnerBillMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerBillRepo extends ServiceImpl<OwnerBillMapper, OwnerBill> {
}
