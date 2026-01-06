package com.homi.service.service.sys;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.MenuTypeEnum;
import com.homi.common.lib.enums.RoleDefaultEnum;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.utils.CollectionUtils;
import com.homi.common.lib.utils.StringUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.mapper.RoleMapper;
import com.homi.model.dao.repo.*;
import com.homi.model.dto.role.RoleMenuAssignDTO;
import com.homi.model.dto.role.RoleQueryDTO;
import com.homi.model.vo.role.RoleVO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.homi.common.lib.utils.CollectionUtils.convertSet;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleMapper roleMapper;
    private final RoleRepo roleRepo;
    private final MenuRepo menuRepo;
    private final RoleMenuRepo roleMenuRepo;

    private final UserRepo userRepo;
    private final UserRoleRepo userRoleRepo;

    public PageVO<RoleVO> listRolePage(RoleQueryDTO queryDTO) {
        Page<RoleVO> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());
        IPage<RoleVO> pageList = roleMapper.selectRolePage(page, queryDTO);

        pageList.getRecords().forEach(item -> {
            User byId = userRepo.getById(item.getCreateBy());
            item.setCreateByName(byId.getNickname());
        });

        PageVO<RoleVO> pageVO = new PageVO<>();
        pageVO.setTotal(pageList.getTotal());
        pageVO.setList(pageList.getRecords());
        pageVO.setCurrentPage(pageList.getCurrent());
        pageVO.setPageSize(pageList.getSize());
        pageVO.setPages(pageList.getPages());

        return pageVO;
    }

    public Long createRole(Role role) {
        validateRoleUniqueness(null, role.getCompanyId(), role.getName(), role.getCode());

        role.setStatus(StatusEnum.ACTIVE.getValue());

        roleMapper.insert(role);
        return role.getId();
    }

    /**
     * 更新角色
     *
     * @param role 角色对象
     * @return 角色ID
     */
    public Long updateRole(Role role) {
        if (Objects.nonNull(RoleDefaultEnum.fromValue(role.getId())) && role.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "系统内置角色无法停用");
        }
        Role exists = roleMapper.selectById(role.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的角色不存在");
        }

        validateRoleUniqueness(role.getId(), role.getCompanyId(), role.getName(), role.getCode());
        roleMapper.updateById(role);
        return role.getId();
    }

    public boolean hasSuperAdmin(List<Long> ids) {
        for (Long id : ids) {
            if (id.equals(RoleDefaultEnum.PLATFORM_SUPER_ADMIN.getId())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 校验角色名称和编码的唯一性
     *
     * @param id   角色ID，用于排除自身
     * @param name 角色名称
     * @param code 角色编码
     */
    private void validateRoleUniqueness(Long id, Long companyId, String name, String code) {
        Role roleName = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getName, name).eq(Role::getCompanyId, companyId));
        if (roleName != null && !roleName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "角色名称不能重复");
        }
        Role roleCode = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, code).eq(Role::getCompanyId, companyId));
        if (roleCode != null && !roleCode.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "角色编码不能重复");
        }
    }

    public Role getRoleById(Long id) {
        return roleMapper.selectById(id);
    }

    public Boolean deleteRoleById(Long id) {
        return roleRepo.removeById(id);
    }

    public List<Role> getSimpleList(RoleQueryDTO queryDTO) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<Role>()
            .like(StringUtils.isNotEmpty(queryDTO.getName()), Role::getName, queryDTO.getName());

        return roleRepo.list(queryWrapper);
    }

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
        List<Menu> menus = menuRepo.getBaseMapper().listRoleMenuByRoles(roleIds, MenuTypeEnum.getButtonList());
        return menus.stream().map(Menu::getAuths).collect(Collectors.toList());
    }

    public void setRolePermission(Long roleId, List<Long> permissions) {
        List<RoleMenu> saveList = new ArrayList<>();
        permissions.forEach(permission -> {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(permission);
            saveList.add(roleMenu);
        });

        roleMenuRepo.saveBatch(saveList);
    }

    public List<Long> getMenuIdsByRoleId(@NotNull(message = "id不能为空") Long id) {
        return roleMenuRepo.listObjs(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id).select(RoleMenu::getMenuId));
    }

    /**
     * 为角色分配菜单
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/27 12:31
     *
     * @param assignDTO 分配DTO
     * @return java.lang.Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignRoleMenu(RoleMenuAssignDTO assignDTO) {
        Long roleId = assignDTO.getRoleId();
        Set<Long> menuIds = assignDTO.getMenuIds();

        Role sysRole = roleRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<Role>().eq(Role::getId, roleId));
        if (Objects.isNull(sysRole)) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "该角色不存在");
        }

        // 为空删除所有
        if (CollUtil.isEmpty(menuIds)) {
            roleMenuRepo.remove(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
            return true;
        }

        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = CollectionUtils.convertSet(roleMenuRepo.list(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId)), RoleMenu::getMenuId);
        // 计算新增和删除的菜单编号
        Set<Long> menuIdList = CollUtil.emptyIfNull(menuIds);
        Collection<Long> createMenuIds = CollUtil.subtract(menuIdList, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIdList);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            roleMenuRepo.saveBatch(CollectionUtils.convertList(createMenuIds, menuId -> {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                return roleMenu;
            }));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            roleMenuRepo.remove(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId).in(RoleMenu::getMenuId, deleteMenuIds));
        }

        return true;
    }

    /**
     * 获取用户角色ID列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/29 22:16
     *
     * @param userId 参数说明
     * @return java.util.List<java.lang.Long>
     */
    public List<Long> getRoleIdsByUser(Long userId) {
        List<UserRole> userRoles = userRoleRepo.list(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userId));
        return userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
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
    public void assignRoleByUserId(Long userId, Set<Long> roleIds) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleRepo.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)),
            UserRole::getRoleId);
        // 计算新增和删除的角色编号
        Set<Long> roleIdList = CollUtil.emptyIfNull(roleIds);
        Collection<Long> createRoleIds = CollUtil.subtract(roleIdList, dbRoleIds);
        Collection<Long> deleteRoleIds = CollUtil.subtract(dbRoleIds, roleIdList);

        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollUtil.isEmpty(createRoleIds)) {
            userRoleRepo.saveBatch(CollectionUtils.convertList(createRoleIds, roleId -> UserRole.builder().userId(userId).roleId(roleId).build()));
            dbRoleIds.addAll(createRoleIds);
        }
        if (!CollUtil.isEmpty(deleteRoleIds)) {
            userRoleRepo.remove(new LambdaQueryWrapper<UserRole>().
                eq(UserRole::getUserId, userId).
                in(UserRole::getRoleId, deleteRoleIds));
            dbRoleIds.removeAll(deleteRoleIds);
        }
    }
}
