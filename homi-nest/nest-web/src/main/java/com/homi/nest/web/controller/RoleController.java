package com.homi.nest.web.controller;


import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.PlatformRole;
import com.homi.model.dto.role.*;
import com.homi.model.nest.vo.NestRoleVO;
import com.homi.nest.service.service.perms.NestRoleService;
import com.homi.nest.web.config.NestLoginManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/nest/role")
@RestController
@RequiredArgsConstructor
public class RoleController {
    private final NestRoleService nestRoleService;

    @PostMapping("/list")
//    @SaCheckPermission("admin:role:list")
    public ResponseResult<PageVO<NestRoleVO>> list(@RequestBody RoleQueryDTO queryDTO) {
        return ResponseResult.ok(nestRoleService.pageRoleList(queryDTO));
    }

    @PostMapping("/options")
//    @SaCheckPermission("admin:role:list")
    public ResponseResult<List<NestRoleVO>> options() {
        return ResponseResult.ok(nestRoleService.listRoleOptions());
    }


    /**
     * 新增角色
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
//    @SaCheckPermission("platform:role:create")
    public ResponseResult<Long> create(@Valid @RequestBody RoleCreateDTO createDTO) {
        PlatformRole platformRole = BeanCopyUtils.copyBean(createDTO, PlatformRole.class);

        assert platformRole != null;
        platformRole.setCreateBy(NestLoginManager.getUserId());
        platformRole.setUpdateBy(NestLoginManager.getUserId());
        platformRole.setCreateTime(DateUtil.date());
        platformRole.setUpdateTime(DateUtil.date());
        platformRole.setStatus(StatusEnum.ACTIVE.getValue());

        return ResponseResult.ok(nestRoleService.createRole(platformRole));
    }

    /**
     * 修改角色
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PostMapping("/update")
//    @SaCheckPermission("system:role:update")
    public ResponseResult<Long> update(@Valid @RequestBody RoleUpdateDTO updateDTO) {
        PlatformRole platformRole = BeanCopyUtils.copyBean(updateDTO, PlatformRole.class);

        assert platformRole != null;
        platformRole.setUpdateBy(NestLoginManager.getUserId());
        platformRole.setUpdateTime(DateUtil.date());

        return ResponseResult.ok(nestRoleService.updateRole(platformRole));
    }

    /**
     * 获取系统管理-角色管理列表
     */
    @PostMapping("/list-all-role")
//    @SaCheckPermission("admin:role:list-all")
    public ResponseResult<List<NestRoleVO>> listAllRole() {
        return ResponseResult.ok(nestRoleService.listAllRole());
    }

    @PostMapping("/role-menu-ids")
//    @SaCheckPermission("admin:role:list-role")
    public ResponseResult<List<Long>> listRole(@RequestBody RoleIdDTO roleId) {
        return ResponseResult.ok(nestRoleService.getMenuIdsByRoleId(roleId.getId()));
    }

    /**
     * 删除角色
     *
     * @param roleIds 主键列表
     * @return 删除结果
     */
    @PostMapping("/delete")
//    @SaCheckPermission("system:role:delete")
    public ResponseResult<Integer> delete(@RequestBody List<Long> roleIds) {
        roleIds.forEach(id -> {
            long count = nestRoleService.getUserCountByRoleId(id);
            if (count > 0) {
                throw new BizException(ResponseCodeEnum.FAIL.getCode(), "角色已绑定用户，无法删除");
            }
        });

        return ResponseResult.ok(nestRoleService.deleteRole(roleIds));
    }

    @PostMapping("/menu/assign")
    public ResponseResult<Void> assignRoleMenu(@RequestBody RoleMenuAssignDTO assignDTO) {
        return ResponseResult.ok(nestRoleService.assignRoleMenu(assignDTO));
    }

    /**
     * 更新角色状态
     *
     * @param updateDTO 实体对象
     * @return 更新结果
     */
    @PostMapping("/status/update")
//    @SaCheckPermission("platform:role:update")
    public ResponseResult<Boolean> updateStatus(@Valid @RequestBody RoleStatusUpdateDTO updateDTO) {
        PlatformRole platformRole = BeanCopyUtils.copyBean(updateDTO, PlatformRole.class);

        assert platformRole != null;
        platformRole.setUpdateBy(NestLoginManager.getUserId());
        platformRole.setUpdateTime(DateUtil.date());

        return ResponseResult.ok(nestRoleService.updateRoleStatus(platformRole));
    }
}

