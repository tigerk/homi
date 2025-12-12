package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.PlatformRoleMenu;
import com.homi.model.dao.mapper.PlatformRoleMenuMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色和菜单关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-22
 */
@Service
public class PlatformRoleMenuRepo extends ServiceImpl<PlatformRoleMenuMapper, PlatformRoleMenu> {

    /**
     * 根据角色id获取角色菜单列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/13 03:18
     *
     * @param roleId 参数说明
     * @return java.util.List<com.homi.model.dao.entity.PlatformRoleMenu>
     */
    public List<PlatformRoleMenu> getRoleMenuListByRoleId(Long roleId) {
        LambdaQueryWrapper<PlatformRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformRoleMenu::getRoleId, roleId);

        return list(queryWrapper);
    }

    public Boolean deleteRoleMenuByRoleId(Long roleId) {
        LambdaQueryWrapper<PlatformRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformRoleMenu::getRoleId, roleId);

        return remove(queryWrapper);
    }
}
