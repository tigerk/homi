package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.PlatformMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface PlatformMenuMapper extends BaseMapper<PlatformMenu> {

    /**
     * 根据角色ID获取菜单（路由菜单或权限菜单）
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/22 20:18
     *
     * @param roleIdList 参数说明
     * @param menuTypes  参数说明
     * @return java.util.List<com.nest.model.entity.PlatformMenu>
     */
    List<PlatformMenu> listRoleMenuByRoles(@Param("roleIdList") List<Long> roleIdList, @Param("menuTypes") List<Integer> menuTypes);
}
