package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.Menu;
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
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据角色ID获取菜单（路由菜单或权限菜单）
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/18 09:41
     *
     * @param roleIdList 参数说明
     * @param isPerms    是否是权限点
     * @return java.util.List<com.homi.model.entity.Menu>
     */
    List<Menu> listRoleMenuByRoles(@Param("roleIdList") List<Long> roleIdList, @Param("isPerms") Boolean isPerms);
}
