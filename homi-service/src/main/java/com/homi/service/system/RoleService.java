package com.homi.service.system;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.dto.role.RoleQueryDTO;
import com.homi.domain.enums.common.MenuTypeEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.RoleDefaultEnum;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.role.RoleVO;
import com.homi.exception.BizException;
import com.homi.dao.entity.Menu;
import com.homi.dao.entity.Role;
import com.homi.dao.entity.RoleMenu;
import com.homi.dao.mapper.RoleMapper;
import com.homi.dao.mapper.RoleMenuMapper;
import com.homi.dao.repo.MenuRepo;
import com.homi.dao.repo.RoleMenuRepo;
import com.homi.dao.repo.RoleRepo;
import com.homi.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final RoleMenuMapper roleMenuMapper;

    public IPage<RoleVO> listRolePage(RoleQueryDTO queryDTO) {
        Page<RoleVO> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());
        return roleMapper.selectRolePage(page, queryDTO);
    }

    public Long saveRole(Role role) {
        validateRoleUniqueness(null, role.getCompanyId(), role.getName(), role.getCode());
        role.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        roleMapper.insert(role);
        return role.getId();
    }

    public Long updateRole(Role role) {
        if (Objects.nonNull(RoleDefaultEnum.fromValue(role.getId())) && role.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "系统内置角色无法停用");
        }
        Role exists = roleMapper.selectById(role.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的角色不存在");
        }

        validateRoleUniqueness(role.getId(), role.getCompanyId(), role.getName(), role.getCode());
        role.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
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
}
