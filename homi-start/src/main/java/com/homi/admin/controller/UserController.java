package com.homi.admin.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.Log;
import com.homi.annotation.RepeatSubmit;
import com.homi.config.MyBatisTenantContext;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.user.*;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.domain.vo.IdNameVO;
import com.homi.model.entity.User;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户表(User)表控制层
 *
 * @author sjh
 * @since 2024-04-25 10:36:46
 */

@RequestMapping("/admin/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    /**
     * 服务对象
     */
    private final UserService userService;

    /**
     * 用户列表
     *
     * @return 所有数据
     */
    @PostMapping("/list")
    @SaCheckPermission("system:user:query")
    public ResponseResult<PageVO<UserVO>> list(@RequestBody UserQueryDTO queryDTO) {
        queryDTO.setCompanyId(MyBatisTenantContext.getCurrentTenant());

        return ResponseResult.ok(userService.pageUserList(queryDTO));
    }

    /**
     * 用户详情
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
     * 新增数据
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @RepeatSubmit
    @Log(title = "用户管理", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> create(@Valid @RequestBody UserCreateDTO createDTO) {
        User user = BeanCopyUtils.copyBean(createDTO, User.class);
        return ResponseResult.ok(userService.createUser(user));
    }

    /**
     * 修改用户
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/update")
    @SaCheckPermission("system:user:update")
    public ResponseResult<Long> update(@Valid @RequestBody UserUpdateDTO updateDTO) {
        User user = BeanCopyUtils.copyBean(updateDTO, User.class);
        user.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(this.userService.updateUser(user));
    }

    /**
     * 修改用户状态
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/updateStatus")
    @SaCheckPermission("system:user:updateStatus")
    public ResponseResult<Long> updateStatus(@Valid @RequestBody UserUpdateStatusDTO updateDTO) {
        User user = BeanCopyUtils.copyBean(updateDTO, User.class);
        user.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(this.userService.updateUser(user));
    }

    /**
     * 删除用户
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @SaCheckPermission("system:user:delete")
    public ResponseResult<Integer> delete(@RequestBody List<Long> idList) {
        return ResponseResult.ok(this.userService.deleteByIds(idList));
    }

    /**
     * 重置密码
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/resetPassword")
    @SaCheckPermission("system:user:resetPwd")
    public ResponseResult<Boolean> resetPassword(@Valid @RequestBody UserResetPwdDTO updateDTO) {
        User user = BeanCopyUtils.copyBean(updateDTO, User.class);
        user.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        this.userService.resetPassword(user);
        return ResponseResult.ok(true);
    }
}

