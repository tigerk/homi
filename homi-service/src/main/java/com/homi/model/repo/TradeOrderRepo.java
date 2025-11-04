package com.homi.model.repo;

import com.homi.model.entity.TradeOrder;
import com.homi.model.mapper.TradeOrderMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 统一交易订单表（租客/房东/平台/第三方支付） 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class TradeOrderRepo extends ServiceImpl<TradeOrderMapper, TradeOrder> {

}
