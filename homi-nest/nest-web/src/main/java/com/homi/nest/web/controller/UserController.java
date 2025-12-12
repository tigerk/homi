package com.homi.nest.web.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.homi.common.lib.annotation.RepeatSubmit;
import com.homi.common.lib.enums.platform.PlatformUserTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.PlatformUser;
import com.homi.model.dto.user.UserQueryDTO;
import com.homi.model.nest.dto.*;
import com.homi.model.nest.vo.NestUserVO;
import com.homi.nest.service.service.perms.NestUserService;
import com.homi.nest.web.config.NestLoginManager;
import com.homi.nest.web.service.NestAuthService;
import com.homi.nest.web.vo.login.NestUserLoginVO;
import com.homi.service.external.qiniu.QiniuClient;
import com.qiniu.common.QiniuException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RequestMapping("/nest/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final NestUserService nestUserService;

    private final QiniuClient qiniuClient;
    private final NestAuthService nestAuthService;

    /**
     * 用户列表
     *
     * @return 所有数据
     */
    @PostMapping("/list")
    public ResponseResult<PageVO<NestUserVO>> list(@RequestBody UserQueryDTO queryDTO) {
        PageVO<NestUserVO> userList = nestUserService.getUserList(queryDTO);
        NestUserLoginVO currentUser = NestLoginManager.getCurrentUser();
        for (NestUserVO user : userList.getList()) {
            try {
                nestAuthService.canUpdateUser(user.getId(), currentUser);
                user.setCanUpdate(Boolean.TRUE);
            } catch (BizException e) {
                user.setCanUpdate(Boolean.FALSE);
            }
        }

        return ResponseResult.ok(userList);
    }

    /**
     * 用户详情
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/detail/{id}")
    @SaCheckPermission("system:user:detail")
    public ResponseResult<NestUserVO> detail(@PathVariable("id") Long id) {
        PlatformUser platformUserById = nestUserService.getUserById(id);
        NestUserVO nestUserVO = BeanCopyUtils.copyBean(platformUserById, NestUserVO.class);

        return ResponseResult.ok(nestUserVO);
    }

    /**
     * 新增数据
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @RepeatSubmit
    public ResponseResult<Long> create(@Valid @RequestBody NestUserCreateDTO createDTO) {
        NestUserLoginVO currentUser = NestLoginManager.getCurrentUser();
        // 只有超管才能创建超管账户
        if (Objects.equals(createDTO.getUserType(), PlatformUserTypeEnum.SUPER_USER.getType())
            && !currentUser.getUserType().equals(PlatformUserTypeEnum.SUPER_USER.getType())) {
            throw new BizException("无权限操作");
        }

        PlatformUser platformUser = BeanCopyUtils.copyBean(createDTO, PlatformUser.class);
        assert platformUser != null;
        platformUser.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        platformUser.setCreateTime(DateUtil.date());
        platformUser.setUsername(createDTO.getPhone());
        return ResponseResult.ok(nestUserService.createUser(platformUser));
    }

    /**
     * 修改用户
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PostMapping("/update")
    public ResponseResult<Long> update(@Valid @RequestBody NestUserUpdateDTO updateDTO) {
        nestAuthService.canUpdateUser(updateDTO.getId(), NestLoginManager.getCurrentUser());

        PlatformUser platformUser = BeanCopyUtils.copyBean(updateDTO, PlatformUser.class);
        platformUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(nestUserService.updateUser(platformUser));
    }

    /**
     * 修改用户状态
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PostMapping("/updateStatus")
    public ResponseResult<Long> updateStatus(@Valid @RequestBody NestUserUpdateStatusDTO updateDTO) {
        nestAuthService.canUpdateUser(updateDTO.getId(), NestLoginManager.getCurrentUser());

        PlatformUser platformUser = BeanCopyUtils.copyBean(updateDTO, PlatformUser.class);
        assert platformUser != null;
        platformUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(this.nestUserService.updateUser(platformUser));
    }

    /**
     * 删除用户
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/delete")
//    @SaCheckPermission("system:user:delete")
    public ResponseResult<Integer> delete(@RequestBody List<Long> idList) {
        idList.forEach(id -> nestAuthService.canUpdateUser(id, NestLoginManager.getCurrentUser()));

        Long currentUserId = NestLoginManager.getCurrentUser().getId();
        if (idList.contains(currentUserId)) {
            throw new BizException("无法删除自身");
        }

        return ResponseResult.ok(this.nestUserService.deleteByIds(idList));
    }

    /**
     * 重置密码
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PostMapping("/resetPassword")
    public ResponseResult<Boolean> resetPassword(@Valid @RequestBody NestUserResetPwdDTO updateDTO) {
        if (Objects.isNull(updateDTO.getId()) || Objects.isNull(updateDTO.getPassword())) {
            throw new BizException("参数异常");
        }

        nestAuthService.canUpdateUser(updateDTO.getId(), NestLoginManager.getCurrentUser());

        PlatformUser platformUser = BeanCopyUtils.copyBean(updateDTO, PlatformUser.class);
        platformUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        platformUser.setUpdateTime(DateUtil.date());
        nestUserService.resetPassword(platformUser);
        return ResponseResult.ok(true);
    }

    /**
     * 更新用户头像
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PostMapping("/updateAvatar")
    public ResponseResult<Boolean> updateAvatar(@Valid @RequestBody NestUpdateAvatarDTO updateDTO) throws QiniuException {
        // 去掉前缀
        String avatar = updateDTO.getAvatar().split(",")[1];

        // 解码成字节数组
        byte[] bytes = Base64.decode(avatar);

        String upload = qiniuClient.upload(bytes, String.format("avatar/%s.png", IdUtil.fastSimpleUUID()));
        updateDTO.setAvatar(upload);

        PlatformUser platformUser = BeanCopyUtils.copyBean(updateDTO, PlatformUser.class);
        platformUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        platformUser.setUpdateTime(DateUtil.date());
        nestUserService.updateAvatar(platformUser);
        return ResponseResult.ok(true);
    }

    /**
     * 根据 userId 查询角色 id 列表
     *
     * @param userId 用户 id
     * @return 角色 id 列表
     */
    @PostMapping("/list-role-ids")
    public ResponseResult<List<Long>> listRoleIds(@RequestBody NestUserIdDTO userId) {
        return ResponseResult.ok(nestUserService.listRoleIds(userId.getUserId()));
    }

    @PostMapping("/role/update")
    public ResponseResult<Boolean> updateRole(@Valid @RequestBody NestUserRoleAssignDTO userRoleAssignDTO) {
        return ResponseResult.ok(nestUserService.updateUserRole(userRoleAssignDTO));
    }
}

