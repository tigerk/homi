package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.RentalOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 统一交易订单表（租客/房东/平台/第三方支付） Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-10
 */
@Mapper
public interface RentalOrderMapper extends BaseMapper<RentalOrder> {

}
