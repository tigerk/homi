package com.homi.service.system;

import com.homi.model.entity.SysMenu;
import com.homi.model.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/18 09:55
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysPermissionService {
    private final SysMenuMapper sysMenuMapper;

    /**
     * 获取按钮权限菜单
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/19 23:35
     *
     * @param roleIds 参数说明
     * @return java.util.Set<java.lang.String>
     */
    public List<String> getMenuPermissionByRoles(List<Long> roleIds) {
        List<SysMenu> sysMenus = sysMenuMapper.listRoleMenuByRoles(roleIds, true);
        return sysMenus.stream().map(SysMenu::getPerms).collect(Collectors.toList());
    }
}
