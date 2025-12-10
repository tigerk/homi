package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.House;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 房源表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {

}
