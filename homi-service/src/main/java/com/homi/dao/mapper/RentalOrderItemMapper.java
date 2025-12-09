package com.homi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.dao.entity.RentalOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 交易订单与账单关联表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-10
 */
@Mapper
public interface RentalOrderItemMapper extends BaseMapper<RentalOrderItem> {

}
