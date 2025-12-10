package com.homi.saas.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dto.role.RoleMenuAssignDTO;
import com.homi.model.dto.user.UserRoleAssignDTO;
import com.homi.service.service.system.PermissionService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("admin/permission")
@Schema(description = "权限管理")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取角色已分配菜单
     *
     * @return 所有数据
     */

    @GetMapping("/getMenuIdList/{roleId}")
    @SaCheckPermission("system:permission:getMenus")
    public ResponseResult<List<Long>> getMenuIdList(@PathVariable("roleId") Long roleId) {
        return ResponseResult.ok(permissionService.getMenuIdListByRole(roleId));
    }

    /**
     * 为角色分配菜单
     */
    @PostMapping("/assignForRole")
    @SaCheckPermission("system:permission:assignMenu")
    public ResponseResult<Void> assignForRole(@Valid @RequestBody RoleMenuAssignDTO assignDTO) {
        permissionService.assignMenu(assignDTO.getRoleId(), assignDTO.getMenuIds());
        return ResponseResult.ok();
    }

    /**
     * 获取用户所分配角色
     *
     * @return 所有数据
     */
    @GetMapping("/getRoleIds/{userId}")
    @SaCheckPermission("system:permission:getRoles")
    public ResponseResult<List<Long>> getRoleIds(@PathVariable("userId") Long userId) {
        return ResponseResult.ok(permissionService.getRoleIdsByUser(userId));
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/assignRoleForUser")
    @SaCheckPermission("system:permission:assignRole")
    public ResponseResult<Void> assignRoleForUser(@Valid @RequestBody UserRoleAssignDTO assignDTO) {
        permissionService.assignRole(assignDTO.getUserId(), assignDTO.getRoleIds());
        return ResponseResult.ok();
    }
}
