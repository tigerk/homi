package com.homi.admin.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.annotation.RepeatSubmit;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.user.*;
import com.homi.domain.vo.user.SysUserVO;
import com.homi.model.entity.SysUser;
import com.homi.service.system.SysUserService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户表(SysUser)表控制层
 *
 * @author sjh
 * @since 2024-04-25 10:36:46
 */

@RequestMapping("/admin/sys/user")
@RestController
@RequiredArgsConstructor
public class SysUserController {
    /**
     * 服务对象
     */
    private final SysUserService sysUserService;

    /**
     * 用户列表
     *
     * @return 所有数据
     */
    @PostMapping("/list")
    @SaCheckPermission("system:user:query")
    public ResponseResult<PageVO<SysUserVO>> list(@RequestBody UserQueryDTO queryDTO) {
        return ResponseResult.ok(sysUserService.getUserList(queryDTO));
    }

    /**
     * 用户详情
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/detail/{id}")
    @SaCheckPermission("system:user:detail")
    public ResponseResult<SysUserVO> detail(@PathVariable("id") Long id) {
        return ResponseResult.ok(sysUserService.getUserById(id));
    }

    /**
     * 新增数据
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @RepeatSubmit
    @SaCheckPermission("system:user:create")
    public ResponseResult<Long> create(@Valid @RequestBody SysUserCreateDTO createDTO) {
        SysUser sysUser = BeanCopyUtils.copyBean(createDTO, SysUser.class);
        sysUser.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(sysUserService.createUser(sysUser));
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
        SysUser sysUser = BeanCopyUtils.copyBean(updateDTO, SysUser.class);
        sysUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(this.sysUserService.updateUser(sysUser));
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
        SysUser sysUser = BeanCopyUtils.copyBean(updateDTO, SysUser.class);
        sysUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(this.sysUserService.updateUser(sysUser));
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
        return ResponseResult.ok(this.sysUserService.deleteByIds(idList));
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
        SysUser sysUser = BeanCopyUtils.copyBean(updateDTO, SysUser.class);
        sysUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        this.sysUserService.resetPassword(sysUser);
        return ResponseResult.ok(true);
    }
}

