package com.homi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.dao.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 客户表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

}
