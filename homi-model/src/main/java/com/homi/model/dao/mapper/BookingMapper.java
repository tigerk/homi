package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 预定/定金表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2026-01-09
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

}
