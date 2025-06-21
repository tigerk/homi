package com.homi.service.system;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.SysMenu;
import com.homi.model.entity.SysRole;
import com.homi.model.entity.SysRoleMenu;
import com.homi.model.entity.SysUserRole;
import com.homi.model.mapper.*;
import com.homi.model.repo.SysRoleMenuRepo;
import com.homi.model.repo.SysUserRoleRepo;
import com.homi.utils.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.homi.utils.CollectionUtils.convertSet;

/**
 * 权限
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/29 23:15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionService {

    private final SysRoleMenuRepo roleMenuRepo;

    private final SysMenuMapper menuMapper;

    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysUserRoleRepo userRoleRepo;

    private final SysNoticeRoleMapper noticeRoleMapper;

    private final SysNoticeUserReadMapper noticeUserReadMapper;

    /**
     * 为角色分配菜单
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/29 22:14
     *
     * @param roleId  参数说明
     * @param menuIds 参数说明
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenu(Long roleId, Set<Long> menuIds) {
        SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getId, roleId));
        if (Objects.isNull(sysRole)) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "该角色不存在");
        }

        if (Objects.equals(sysRole.getRoleCode(), "admin")) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "超级管理员拥有所有权限，无需分配");
        }

        // 为空删除所有
        if (CollUtil.isEmpty(menuIds)) {
            sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
            return;
        }

        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = convertSet(sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().
                eq(SysRoleMenu::getRoleId, roleId)), SysRoleMenu::getMenuId);
        // 计算新增和删除的菜单编号
        Set<Long> menuIdList = CollUtil.emptyIfNull(menuIds);
        Collection<Long> createMenuIds = CollUtil.subtract(menuIdList, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIdList);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            roleMenuRepo.saveBatch(CollectionUtils.convertList(createMenuIds, menuId -> {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setRoleId(roleId);
                sysRoleMenu.setMenuId(menuId);
                return sysRoleMenu;
            }));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().
                    eq(SysRoleMenu::getRoleId, roleId).in(SysRoleMenu::getMenuId, deleteMenuIds));
        }
    }

    /**
     * 获取角色所分配菜单ID
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/29 22:16
     *
     * @param roleId 参数说明
     * @return java.util.List<java.lang.Long>
     */
    public List<Long> getMenuIdListByRole(Long roleId) {
        List<SysRoleMenu> list = roleMenuRepo.list(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        return list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    public List<Long> getRoleIdsByUser(Long userId) {
        List<SysUserRole> sysUserRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userId));
        return sysUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    /**
     * 给用户分配角色
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/29 23:17
     *
     * @param userId  参数说明
     * @param roleIds 参数说明
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRole(Long userId, Set<Long> roleIds) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)),
                SysUserRole::getRoleId);
        // 计算新增和删除的角色编号
        Set<Long> roleIdList = CollUtil.emptyIfNull(roleIds);
        Collection<Long> createRoleIds = CollUtil.subtract(roleIdList, dbRoleIds);
        Collection<Long> deleteRoleIds = CollUtil.subtract(dbRoleIds, roleIdList);

        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollUtil.isEmpty(createRoleIds)) {
            userRoleRepo.saveBatch(CollectionUtils.convertList(createRoleIds, roleId -> SysUserRole.builder().userId(userId).roleId(roleId).build()));
            dbRoleIds.addAll(createRoleIds);
        }
        if (!CollUtil.isEmpty(deleteRoleIds)) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().
                    eq(SysUserRole::getUserId, userId).
                    in(SysUserRole::getRoleId, deleteRoleIds));
            dbRoleIds.removeAll(deleteRoleIds);
        }
    }

    /**
     * 获取按钮权限菜单
     *
     * @param roleIds 角色Ids
     * @return
     */
    public List<String> getMenuPermissionByRoles(List<Long> roleIds) {
        List<SysMenu> sysMenus = menuMapper.listRoleMenuByRoles(roleIds, true);
        return sysMenus.stream().map(SysMenu::getAuths).collect(Collectors.toList());
    }
}
