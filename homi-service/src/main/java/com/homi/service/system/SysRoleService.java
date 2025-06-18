package com.homi.service.system;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.dto.role.RoleQueryDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.RoleDefaultEnum;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.role.SysRoleVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysMenu;
import com.homi.model.entity.SysRole;
import com.homi.model.entity.SysRoleMenu;
import com.homi.model.mapper.SysRoleMapper;
import com.homi.model.mapper.SysRoleMenuMapper;
import com.homi.model.repo.SysMenuRepo;
import com.homi.model.repo.SysRoleMenuRepo;
import com.homi.model.repo.SysRoleRepo;
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
public class SysRoleService {
    private final SysRoleMapper sysRoleMapper;

    private final SysRoleRepo sysRoleRepo;
    private final SysMenuRepo sysMenuRepo;
    private final SysRoleMenuRepo sysRoleMenuRepo;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    public IPage<SysRoleVO> listRolePage(RoleQueryDTO queryDTO) {
        Page<SysRoleVO> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());
        return sysRoleMapper.selectRolePage(page, queryDTO);
    }

    public Long createRole(SysRole sysRole) {
        validateRoleUniqueness(null, sysRole.getCompanyId(), sysRole.getRoleName(), sysRole.getRoleCode());
        sysRole.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysRoleMapper.insert(sysRole);
        return sysRole.getId();
    }

    public Long updateRole(SysRole sysRole) {
        if (Objects.nonNull(RoleDefaultEnum.fromValue(sysRole.getId())) && sysRole.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "系统内置角色无法停用");
        }
        SysRole exists = sysRoleMapper.selectById(sysRole.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的角色不存在");
        }

        validateRoleUniqueness(sysRole.getId(), sysRole.getCompanyId(), sysRole.getRoleName(), sysRole.getRoleCode());
        sysRole.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysRoleMapper.updateById(sysRole);
        return sysRole.getId();
    }

    public boolean hasSuperAdmin(List<Long> ids) {
        for (Long id : ids) {
            if (id.equals(RoleDefaultEnum.SUPERADMIN.getId())) {
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
        SysRole sysRoleName = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleName, name).eq(SysRole::getCompanyId, companyId));
        if (sysRoleName != null && !sysRoleName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "角色名称不能重复");
        }
        SysRole sysRoleCode = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code).eq(SysRole::getCompanyId, companyId));
        if (sysRoleCode != null && !sysRoleCode.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "角色编码不能重复");
        }
    }

    public SysRole getRoleById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    public Boolean deleteRoleById(Long id) {

        return sysRoleRepo.removeById(id);
    }

    public List<SysRole> getSimpleList(RoleQueryDTO queryDTO) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.isNotEmpty(queryDTO.getRoleName()), SysRole::getRoleName, queryDTO.getRoleName());

        return sysRoleRepo.list(queryWrapper);
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
        List<SysMenu> sysMenus = sysMenuRepo.getBaseMapper().listRoleMenuByRoles(roleIds, true);
        return sysMenus.stream().map(SysMenu::getPerms).collect(Collectors.toList());
    }

    public void setRolePermission(Long roleId, List<Long> permissions) {
        List<SysRoleMenu> saveList = new ArrayList<>();
        permissions.forEach(permission -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenu.setMenuId(permission);
            saveList.add(sysRoleMenu);
        });

        sysRoleMenuRepo.saveBatch(saveList);
    }
}
