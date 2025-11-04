package com.homi.model.repo;

import com.homi.model.entity.TradeOrderBill;
import com.homi.model.mapper.TradeOrderBillMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 交易订单与账单关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class TradeOrderBillRepo extends ServiceImpl<TradeOrderBillMapper, TradeOrderBill> {

}
