package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OperationLog;
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
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}
