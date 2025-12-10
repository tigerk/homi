package com.homi.model.dao.repo;

import com.homi.model.dao.entity.RentalOrderItem;
import com.homi.model.dao.mapper.RentalOrderItemMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 交易订单与账单关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-10
 */
@Service
public class RentalOrderItemRepo extends ServiceImpl<RentalOrderItemMapper, RentalOrderItem> {

}
