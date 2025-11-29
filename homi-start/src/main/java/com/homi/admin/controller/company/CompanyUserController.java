package com.homi.admin.controller.company;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.annotation.Log;
import com.homi.annotation.RepeatSubmit;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.user.UserCreateDTO;
import com.homi.domain.dto.user.UserQueryDTO;
import com.homi.domain.dto.user.UserResetPwdDTO;
import com.homi.domain.dto.user.UserUpdateStatusDTO;
import com.homi.domain.enums.common.UserTypeEnum;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.vo.company.user.UserCreateVO;
import com.homi.domain.vo.company.user.UserVO;
import com.homi.model.entity.UserCompany;
import com.homi.model.entity.User;
import com.homi.service.company.CompanyUserService;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 用户表(User)表控制层
 *
 * @author sjh
 * @since 2024-04-25 10:36:46
 */

@RequestMapping("/admin/company/user")
@RestController
@RequiredArgsConstructor
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
    @SaCheckPermission("system:user:query")
    public ResponseResult<PageVO<UserVO>> list(@RequestBody UserQueryDTO queryDTO) {
        return ResponseResult.ok(companyUserService.pageUserList(queryDTO));
    }

    /**
     * 用户详情，在公司下只能查看公司下的用户
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/detail/{id}")
    @SaCheckPermission("system:user:detail")
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
    @SaCheckPermission("system:user:updateStatus")
    public ResponseResult<Long> updateStatus(@Valid @RequestBody UserUpdateStatusDTO updateDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        UserCompany userCompanyById = companyUserService.getCompanyUserById(updateDTO.getCompanyUserId());
        if (userCompanyById.getUserType().equals(UserTypeEnum.COMPANY_ADMIN.getType())) {
            return ResponseResult.fail(ResponseCodeEnum.AUTHORIZED);
        }

        updateDTO.setCompanyId(loginUser.getCurCompanyId());
        Long updated = companyUserService.updateUserUserStatus(updateDTO);
        if (updated > 0) {
            // 修改状态后，被修改用户需要重新登录
            User userById = userService.getUserById(userCompanyById.getUserId());
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
    @SaCheckPermission("system:user:delete")
    public ResponseResult<Integer> delete(@RequestBody List<Long> idList) {
        List<User> userByIds = userService.getUserByIds(idList);

        Integer deleted = companyUserService.deleteByIds(idList);

        if (deleted > 0) {
            // 删除后，被删除用户需要重新登录
            userByIds.forEach(user -> {
                authService.kickUserByUsername(user.getUsername());
            });
        }


        return ResponseResult.ok(deleted);
    }

    /**
     * 重置密码，在各个公司都可以给用户重置密码
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/resetPassword")
    @SaCheckPermission("system:user:resetPwd")
    public ResponseResult<Boolean> resetPassword(@Valid @RequestBody UserResetPwdDTO updateDTO) {
        User user = BeanCopyUtils.copyBean(updateDTO, User.class);
        assert user != null;
        user.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        userService.resetPassword(user);
        return ResponseResult.ok(true);
    }
}

