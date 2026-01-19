package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.Delivery;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 通用物业交割主表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2026-01-19
 */
@Mapper
public interface DeliveryMapper extends BaseMapper<Delivery> {

}
