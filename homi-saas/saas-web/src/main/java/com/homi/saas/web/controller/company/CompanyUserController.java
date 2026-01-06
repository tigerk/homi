package com.homi.saas.web.controller.company;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.annotation.RepeatSubmit;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.enums.SaasUserTypeEnum;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.CompanyUser;
import com.homi.model.dao.entity.User;
import com.homi.model.dto.company.user.CompanyUserIdDTO;
import com.homi.model.dto.company.user.CompanyUserRoleAssignDTO;
import com.homi.model.dto.user.UserCreateDTO;
import com.homi.model.dto.user.UserQueryDTO;
import com.homi.model.dto.user.UserResetPwdDTO;
import com.homi.model.dto.user.UserUpdateStatusDTO;
import com.homi.model.vo.company.user.UserCreateVO;
import com.homi.model.vo.company.user.UserVO;
import com.homi.saas.web.auth.service.AuthService;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.company.CompanyUserService;
import com.homi.service.service.sys.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 用户表(User)表控制层
 *
 * @author tigerk
 * @since 2024-04-25 10:36:46
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/company/user")
public class CompanyUserController {
    /**
     * 服务对象
     */
    private final UserService userService;

    private final CompanyUserService companyUserService;

    private final AuthService authService;

    /**
     * 用户列表，在公司下只能查看公司下的用户
     *
     * @return 所有数据
     */
    @PostMapping("/list")
    @SaCheckPermission("sys:user:query")
    public ResponseResult<PageVO<UserVO>> list(@RequestBody UserQueryDTO queryDTO) {
        queryDTO.setCompanyId(LoginManager.getCurrentUser().getCurCompanyId());

        return ResponseResult.ok(companyUserService.pageUserList(queryDTO));
    }

    /**
     * 用户详情，在公司下只能查看公司下的用户
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/detail/{id}")
    @SaCheckPermission("sys:user:detail")
    public ResponseResult<UserVO> detail(@PathVariable("id") Long id) {
        User userById = userService.getUserById(id);
        UserVO userVO = BeanCopyUtils.copyBean(userById, UserVO.class);

        return ResponseResult.ok(userVO);
    }

    /**
     * 新增公司下的用户
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @RepeatSubmit
    @Log(title = "用户管理", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<UserCreateVO> create(@Valid @RequestBody UserCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCompanyId(loginUser.getCurCompanyId());
        createDTO.setUsername(createDTO.getPhone());

        if (Objects.isNull(createDTO.getCompanyUserId())) {
            createDTO.setUpdateBy(loginUser.getId());
            return ResponseResult.ok(companyUserService.createUser(createDTO));
        } else {
            return ResponseResult.ok(companyUserService.updateUser(createDTO));
        }
    }

    /**
     * 修改公司下的用户状态
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PostMapping("/updateStatus")
    @SaCheckPermission("sys:user:updateStatus")
    public ResponseResult<Long> updateStatus(@Valid @RequestBody UserUpdateStatusDTO updateDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        CompanyUser companyUserById = companyUserService.getCompanyUserById(updateDTO.getCompanyUserId());
        if (companyUserById.getUserType().equals(SaasUserTypeEnum.COMPANY_ADMIN.getType())) {
            return ResponseResult.fail(ResponseCodeEnum.AUTHORIZED.getCode(), "公司管理员不能被禁用");
        }

        updateDTO.setCompanyId(loginUser.getCurCompanyId());
        Long updated = companyUserService.updateUserUserStatus(updateDTO);
        if (updated > 0) {
            // 修改状态后，被修改用户需要重新登录
            User userById = userService.getUserById(companyUserById.getUserId());
            authService.kickUserByUsername(userById.getUsername());
        }

        return ResponseResult.ok(updated);
    }

    /**
     * 删除公司下的用户
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaCheckPermission("sys:user:delete")
    public ResponseResult<Integer> delete(@RequestBody List<Long> idList) {
        // 删除后，被删除用户需要重新登录
        AtomicReference<Integer> deleted = new AtomicReference<>(0);
        idList.forEach(companyUserId -> {
            User user = companyUserService.deleteCompanyUser(companyUserId);
            authService.kickUserByUsername(user.getUsername());
            deleted.getAndSet(deleted.get() + 1);
        });

        return ResponseResult.ok(deleted.get());
    }

    /**
     * 重置密码，在各个公司都可以给用户重置密码
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/resetPassword")
    @SaCheckPermission("sys:user:resetPwd")
    public ResponseResult<Boolean> resetPassword(@Valid @RequestBody UserResetPwdDTO updateDTO) {
        User user = BeanCopyUtils.copyBean(updateDTO, User.class);
        assert user != null;
        user.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        userService.resetPassword(user);
        return ResponseResult.ok(true);
    }

    @PostMapping("list-role-ids")
    public ResponseResult<List<Long>> listRoleIds(@RequestBody CompanyUserIdDTO companyUserIdDTO) {
        return ResponseResult.ok(JSONUtil.toList(companyUserService.getCompanyUserById(companyUserIdDTO.getCompanyUserId()).getRoles(), Long.class));
    }

    /**
     * 为公司下的用户分配角色
     *
     * @param roleAssignDTO 角色分配DTO
     * @return 分配结果
     */
    @PostMapping("/role/assign")
    public ResponseResult<Boolean> assignUserRole(@RequestBody CompanyUserRoleAssignDTO roleAssignDTO) {
        return ResponseResult.ok(companyUserService.assignRolesByUserId(roleAssignDTO));
    }
}

