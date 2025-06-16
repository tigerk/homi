package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.SysMenu;
import com.homi.model.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Service
public class SysMenuRepo extends ServiceImpl<SysMenuMapper, SysMenu> {
    /**
     * 根据角色查询菜单
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/18 09:39
     *
     * @param roleIdList 参数说明
     * @param isPerms    是否是权限点
     * @return java.util.List<com.homi.model.entity.SysMenu>
     */
    public List<SysMenu> listRoleMenuByRoles(List<Long> roleIdList, Boolean isPerms) {
        return this.baseMapper.listRoleMenuByRoles(roleIdList, isPerms);
    }
}
