package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 系统访问记录 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

}
