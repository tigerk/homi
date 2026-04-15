package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerSettlementBill;
import com.homi.model.dao.mapper.OwnerSettlementBillMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OwnerSettlementBillRepo extends ServiceImpl<OwnerSettlementBillMapper, OwnerSettlementBill> {
}
