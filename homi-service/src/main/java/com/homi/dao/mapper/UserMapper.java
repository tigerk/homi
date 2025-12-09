package com.homi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
