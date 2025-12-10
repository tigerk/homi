package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.MenuTypeEnum;
import com.homi.model.dao.entity.PlatformMenu;
import com.homi.model.dao.entity.PlatformRole;
import com.homi.model.dao.mapper.PlatformMenuMapper;
import com.homi.model.dao.mapper.PlatformRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-22
 */
@Service
@RequiredArgsConstructor
public class PlatformRoleRepo extends ServiceImpl<PlatformRoleMapper, PlatformRole> {
    private final PlatformMenuMapper platformMenuMapper;

    /**
     * 根据id 列表获取角色列表
     *
     * @param roleIdList 角色id列表
     * @return 角色列表
     */
    public List<PlatformRole> getRoleListByIdList(List<Long> roleIdList) {
        return getBaseMapper().selectList(new LambdaQueryWrapper<PlatformRole>().in(PlatformRole::getId, roleIdList));
    }

    /**
     * 根据角色id列表获取权限点
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/22 20:25
     *
     * @param roleIds 参数说明
     * @return java.util.List<java.lang.String>
     */
    public List<String> getMenuPermissionByRoles(List<Long> roleIds) {
        List<PlatformMenu> platformMenus = platformMenuMapper.listRoleMenuByRoles(roleIds, MenuTypeEnum.getButtonList());
        return platformMenus.stream().map(PlatformMenu::getAuths).collect(Collectors.toList());
    }
}
