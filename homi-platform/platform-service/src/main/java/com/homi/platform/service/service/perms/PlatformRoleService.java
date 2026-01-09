package com.homi.platform.service.service.perms;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.CollectionUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.PlatformRole;
import com.homi.model.dao.entity.PlatformRoleMenu;
import com.homi.model.dao.entity.PlatformUserRole;
import com.homi.model.dao.repo.PlatformRoleMenuRepo;
import com.homi.model.dao.repo.PlatformRoleRepo;
import com.homi.model.dao.repo.PlatformUserRoleRepo;
import com.homi.model.role.dto.RoleMenuAssignDTO;
import com.homi.model.role.dto.RoleQueryDTO;
import com.homi.model.platform.vo.PlatformRoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用于 domix-platform
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/25
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformRoleService {
    private final PlatformRoleRepo platformRoleRepo;
    private final PlatformRoleMenuRepo platformRoleMenuRepo;
    private final PlatformUserRoleRepo platformUserRoleRepo;

    public List<PlatformRoleVO> listAllRole() {
        return platformRoleRepo.list().stream().map(r -> BeanCopyUtils.copyBean(r, PlatformRoleVO.class)).collect(Collectors.toList());
    }

    public PageVO<PlatformRoleVO> pageRoleList(RoleQueryDTO query) {
        Page<PlatformRole> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<PlatformRole> queryWrapper = new LambdaQueryWrapper<>();

        if (CharSequenceUtil.isNotBlank(query.getCode())) {
            queryWrapper.eq(PlatformRole::getCode, query.getCode());
        }

        if (CharSequenceUtil.isNotBlank(query.getName())) {
            queryWrapper.like(PlatformRole::getName, "%" + query.getName() + "%");
        }

        if (Objects.nonNull(query.getStatus())) {
            queryWrapper.eq(PlatformRole::getStatus, query.getStatus());
        }

        queryWrapper.orderByDesc(PlatformRole::getCreateTime);

        IPage<PlatformRole> userVOPage = platformRoleRepo.page(page, queryWrapper);

        List<PlatformRoleVO> records = userVOPage.getRecords().stream().map(user -> BeanCopyUtils.copyBean(user, PlatformRoleVO.class)).collect(Collectors.toList());

        PageVO<PlatformRoleVO> pageVO = new PageVO<>();
        pageVO.setTotal(userVOPage.getTotal());
        pageVO.setList(records);
        pageVO.setCurrentPage(userVOPage.getCurrent());
        pageVO.setPageSize(userVOPage.getSize());
        pageVO.setPages(userVOPage.getPages());

        return pageVO;
    }

    public long getUserCountByRoleId(Long roleId) {
        LambdaQueryWrapper<PlatformUserRole> queryWrapper = new LambdaQueryWrapper<PlatformUserRole>().eq(PlatformUserRole::getRoleId, roleId);
        return platformUserRoleRepo.count(queryWrapper);
    }

    /**
     * 删除角色列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/13 03:27
     *
     * @param roleIds 参数说明
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(List<Long> roleIds) {
        roleIds.forEach(this::deleteRoleById);

        return roleIds.size();
    }

    /**
     * 删除角色
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/27 12:31
     *
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleById(Long roleId) {
        platformRoleRepo.getBaseMapper().deleteById(roleId);
        platformRoleMenuRepo.deleteRoleMenuByRoleId(roleId);
    }

    public Long createRole(PlatformRole platformRole) {
        validateRoleUniqueness(null, platformRole.getName(), platformRole.getCode());
        platformRole.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        platformRoleRepo.getBaseMapper().insert(platformRole);
        return platformRole.getId();
    }

    /**
     * 校验角色名称和编码的唯一性
     *
     * @param id   角色ID，用于排除自身
     * @param name 角色名称
     * @param code 角色编码
     */
    private void validateRoleUniqueness(Long id, String name, String code) {
        PlatformRole platformRoleName = platformRoleRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<PlatformRole>().eq(PlatformRole::getName, name));
        if (platformRoleName != null && !platformRoleName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "角色名称不能重复");
        }
        PlatformRole platformRoleCode = platformRoleRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<PlatformRole>().eq(PlatformRole::getCode, code));
        if (platformRoleCode != null && !platformRoleCode.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "角色编码不能重复");
        }
    }

    /**
     * 修改角色
     *
     * @param platformRole 角色信息
     * @return 修改结果
     */
    public Long updateRole(PlatformRole platformRole) {
        PlatformRole exists = platformRoleRepo.getBaseMapper().selectById(platformRole.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的角色不存在");
        }

        validateRoleUniqueness(platformRole.getId(), platformRole.getName(), platformRole.getCode());

        platformRoleRepo.getBaseMapper().updateById(platformRole);
        return platformRole.getId();
    }

    /**
     * 修改角色状态
     *
     * @param platformRole 角色信息
     * @return 修改结果
     */
    public Boolean updateRoleStatus(PlatformRole platformRole) {
        PlatformRole exists = platformRoleRepo.getBaseMapper().selectById(platformRole.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的角色不存在");
        }

        exists.setStatus(platformRole.getStatus());
        exists.setUpdateBy(platformRole.getUpdateBy());
        exists.setUpdateTime(platformRole.getUpdateTime());
        platformRoleRepo.getBaseMapper().updateById(exists);

        return true;
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<PlatformRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformRoleMenu::getRoleId, roleId);

        return platformRoleMenuRepo.list(queryWrapper)
            .stream()
            .map(PlatformRoleMenu::getMenuId)
            .collect(Collectors.toList());
    }

    /**
     * 为角色分配菜单
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/27 12:31
     *
     * @param assignDTO 分配DTO
     * @return java.lang.Void
     */
    @Transactional(rollbackFor = Exception.class)
    public Void assignRoleMenu(RoleMenuAssignDTO assignDTO) {
        Long roleId = assignDTO.getRoleId();
        Set<Long> menuIds = assignDTO.getMenuIds();

        PlatformRole platformRole = platformRoleRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<PlatformRole>().eq(PlatformRole::getId, roleId));
        if (Objects.isNull(platformRole)) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "该角色不存在");
        }

        if (Objects.equals(platformRole.getCode(), "admin")) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "超级管理员拥有所有权限，无需分配");
        }

        // 为空删除所有
        if (CollUtil.isEmpty(menuIds)) {
            platformRoleMenuRepo.getBaseMapper().delete(new LambdaQueryWrapper<PlatformRoleMenu>().eq(PlatformRoleMenu::getRoleId, roleId));
            return null;
        }

        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = CollectionUtils.convertSet(platformRoleMenuRepo.getBaseMapper().selectList(new LambdaQueryWrapper<PlatformRoleMenu>().
            eq(PlatformRoleMenu::getRoleId, roleId)), PlatformRoleMenu::getMenuId);
        // 计算新增和删除的菜单编号
        Set<Long> menuIdList = CollUtil.emptyIfNull(menuIds);
        Collection<Long> createMenuIds = CollUtil.subtract(menuIdList, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIdList);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            platformRoleMenuRepo.saveBatch(CollectionUtils.convertList(createMenuIds, menuId -> {
                PlatformRoleMenu platformRoleMenu = new PlatformRoleMenu();
                platformRoleMenu.setRoleId(roleId);
                platformRoleMenu.setMenuId(menuId);
                return platformRoleMenu;
            }));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            platformRoleMenuRepo.getBaseMapper().delete(new LambdaQueryWrapper<PlatformRoleMenu>().
                eq(PlatformRoleMenu::getRoleId, roleId).in(PlatformRoleMenu::getMenuId, deleteMenuIds));
        }

        return null;
    }

    public List<PlatformRoleVO> listRoleOptions() {
        LambdaQueryWrapper<PlatformRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PlatformRole::getCreateTime);

        return platformRoleRepo.list(queryWrapper)
            .stream().map(role -> BeanCopyUtils.copyBean(role, PlatformRoleVO.class)).collect(Collectors.toList());
    }
}
