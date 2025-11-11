package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.RentalPayment;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 统一交易流水表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-10
 */
@Mapper
public interface RentalPaymentMapper extends BaseMapper<RentalPayment> {

}
