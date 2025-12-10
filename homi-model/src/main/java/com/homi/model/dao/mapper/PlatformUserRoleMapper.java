package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.PlatformUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户和角色关联表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-22
 */
@Mapper
public interface PlatformUserRoleMapper extends BaseMapper<PlatformUserRole> {

}
