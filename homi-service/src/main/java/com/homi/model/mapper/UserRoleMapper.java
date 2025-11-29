package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户和角色关联表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

}
