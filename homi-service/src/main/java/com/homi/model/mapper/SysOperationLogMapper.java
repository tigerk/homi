package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 操作日志记录表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

}
