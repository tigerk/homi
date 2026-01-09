package com.homi.saas.web.controller.sys;


import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Role;
import com.homi.model.user.UserQueryDTO;
import com.homi.model.company.vo.user.UserVO;
import com.homi.model.role.dto.*;
import com.homi.model.role.vo.RoleSimpleVO;
import com.homi.model.role.vo.RoleVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.saas.web.role.RoleConvert;
import com.homi.service.service.company.CompanyUserService;
import com.homi.service.service.sys.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 角色信息表(Role)表控制层
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/28 18:55
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/sys/role")
public class SysRoleController {
    private final RoleService roleService;
    private final CompanyUserService companyUserService;

    /**
     * 查询角色列表
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @PostMapping("/list")
//    @SaCheckPermission("sys:role:query")
    public ResponseResult<PageVO<RoleVO>> selectPage(RoleQueryDTO queryDTO) {
        return ResponseResult.ok(roleService.listRolePage(queryDTO));
    }

    /**
     * 新增角色
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
//    @SaCheckPermission("sys:role:create")
    public ResponseResult<Long> save(@Valid @RequestBody RoleCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        Role role = BeanCopyUtils.copyBean(createDTO, Role.class);
        assert role != null;

        // 更新 or 创建
        if (Objects.nonNull(createDTO.getId())) {
            role.setUpdateBy(loginUser.getId());
            return ResponseResult.ok(roleService.updateRole(role));
        } else {
            role.setCreateBy(loginUser.getId());
            return ResponseResult.ok(roleService.createRole(role));
        }
    }

    /**
     * 删除角色
     *
     * @return 删除结果
     */
    @PostMapping("/delete")
//    @SaCheckPermission("sys:role:delete")
    public ResponseResult<Boolean> delete(@RequestBody RoleIdDTO deleteDTO) {
        Long id = deleteDTO.getId();

        UserQueryDTO userQueryDTO = UserQueryDTO.builder().roleId(id)
            .companyId(LoginManager.getCurrentUser().getCurCompanyId())
            .build();
        userQueryDTO.setCurrentPage(1L);
        userQueryDTO.setPageSize(1L);
        long count = companyUserService.pageUserList(userQueryDTO).getList().size();

        if (count > 0) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "该角色已绑定用户，无法删除，若要删除请先解绑用户");
        }

        return ResponseResult.ok(roleService.deleteRoleById(id));
    }

    /**
     * 查询所有简单角色数据
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @PostMapping("/list/all")
//    @SaCheckPermission("sys:role:listSimpleAll")
    public ResponseResult<List<RoleSimpleVO>> listSimpleAll(RoleQueryDTO queryDTO) {
        List<Role> list = roleService.getSimpleList(queryDTO);
        return ResponseResult.ok(RoleConvert.INSTANCE.convertSimpleList(list));
    }

    @PostMapping("/menu-ids")
//    @SaCheckPermission("sys:role:menuIds")
    public ResponseResult<List<Long>> getMenuIds(@RequestBody RoleIdDTO roleIdDTO) {
        return ResponseResult.ok(roleService.getMenuIdsByRoleId(roleIdDTO.getId()));
    }

    @PostMapping("/menu/assign")
//    @SaCheckPermission("sys:role:menuIds")
    public ResponseResult<Boolean> assignRoleMenu(@RequestBody RoleMenuAssignDTO roleMenuAssignDTO) {
        return ResponseResult.ok(roleService.assignRoleMenu(roleMenuAssignDTO));
    }

    /**
     * 查询角色下的用户列表
     *
     * @param roleIdDTO 查询实体
     * @return 所有数据
     */
    @PostMapping("/user/list")
    public ResponseResult<List<UserVO>> getUserIdsByRoleId(@RequestBody RoleIdDTO roleIdDTO) {
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setRoleId(roleIdDTO.getId());
        queryDTO.setCompanyId(LoginManager.getCurrentUser().getCurCompanyId());

        queryDTO.setCurrentPage(1L);
        queryDTO.setPageSize(9999L);

        return ResponseResult.ok(companyUserService.pageUserList(queryDTO).getList());
    }

    /**
     * 解绑角色下的用户
     *
     * @param unbindDTO 查询实体
     * @return 所有数据
     */
    @PostMapping("user/unbind")
    public ResponseResult<Boolean> unbindRoleCompanyUser(@RequestBody RoleUserUnbindDTO unbindDTO) {
        return ResponseResult.ok(companyUserService.unbindRoleCompanyUser(unbindDTO));
    }
}

