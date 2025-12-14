package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 租客信息表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
