package com.homi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.dao.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 角色和菜单关联表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

}
