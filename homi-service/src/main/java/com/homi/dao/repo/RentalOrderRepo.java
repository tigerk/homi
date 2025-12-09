package com.homi.dao.repo;

import com.homi.dao.entity.RentalOrder;
import com.homi.dao.mapper.RentalOrderMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 统一交易订单表（租客/房东/平台/第三方支付） 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-10
 */
@Service
public class RentalOrderRepo extends ServiceImpl<RentalOrderMapper, RentalOrder> {

}
