package com.homi.service.system;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.user.UserQueryDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.user.UserVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysUserRole;
import com.homi.model.entity.User;
import com.homi.model.mapper.SysUserRoleMapper;
import com.homi.model.mapper.UserMapper;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.homi.domain.constant.ConfigCacheKeyConstants.USER_DEFAULT_AVATAR;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysConfigService sysConfigService;

    private final SysRoleService roleService;

    @Value("${default-avatar}")
    private String defaultAvatar;

    @Transactional(rollbackFor = Exception.class)
    public Long createUser(User user) {
        validateUserUniqueness(null, user.getUsername(), user.getEmail(), user.getPhone());
        user.setAvatar(sysConfigService.getConfigValueByKey(USER_DEFAULT_AVATAR));
        // 密码加密
        user.setPassword(SaSecureUtil.md5(user.getPassword()));
        user.setCreateTime(DateUtil.date());

        if (Objects.isNull(user.getAvatar())) {
            user.setAvatar(defaultAvatar);
        }

        userMapper.insert(user);

        return user.getId();
    }

    public Long updateUser(User user) {
        // 是否存在
        validateUserExists(user.getId());
        // 有冻结的行为
        if (user.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            if (Long.valueOf(StpUtil.getLoginId().toString()).equals(user.getId())) {
                throw new BizException("无法冻结自身");
            }
            List<Long> roleIdList = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, user.getId())).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            if (roleService.hasSuperAdmin(roleIdList)) {
                throw new BizException("无法冻结超级管理员");
            }
        }
        // 邮箱这些是否有重复
        validateUserUniqueness(user.getId(), user.getUsername(), user.getEmail(), user.getPhone());

        user.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        userMapper.updateById(user);
        return user.getId();
    }

    public PageVO<UserVO> getUserList(UserQueryDTO query) {
        Page<UserVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        IPage<UserVO> userVOPage = userMapper.selectUserList(page, query);

        PageVO<UserVO> pageVO = new PageVO<>();
        pageVO.setTotal(userVOPage.getTotal());
        pageVO.setList(userVOPage.getRecords());
        pageVO.setCurrentPage(userVOPage.getCurrent());
        pageVO.setPageSize(userVOPage.getSize());
        pageVO.setPages(userVOPage.getPages());

        return pageVO;
    }

    public Integer deleteByIds(List<Long> idList) {
        Long loginId = Long.valueOf(StpUtil.getLoginId().toString());
        for (Long id : idList) {
            if (id.equals(loginId)) {
                throw new BizException("无法删除自身");
            }
        }
        userMapper.deleteBatchIds(idList);
        return idList.size();
    }

    public void resetPassword(User sysUser) {
        if (Objects.isNull(sysUser.getId()) || Objects.isNull(sysUser.getPassword())) {
            throw new BizException("参数异常");
        }

        User user = validateUserExists(sysUser.getId());
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        user.setPassword(SaSecureUtil.md5(sysUser.getPassword()));
        userMapper.updateById(user);
    }

    private User validateUserExists(Long id) {
        if (id == null) {
            return null;
        }
        User user = userMapper.selectById(id);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /**
     * 校验用户名的唯一性
     *
     * @param id   用户ID，用于排除自身
     * @param name 用户名
     */
    private void validateUserUniqueness(Long id, String name, String email, String phone) {
        User userName = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, name));
        if (userName != null && !userName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该用户名已存在");
        }
        if (CharSequenceUtil.isNotBlank(email)) {
            User userEmail = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
            if (userEmail != null && !userEmail.getId().equals(id)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该邮箱已被用户：" + userEmail.getUsername() + "绑定");
            }
        }
        if (CharSequenceUtil.isNotBlank((phone))) {
            User userPhone = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
            if (userPhone != null && !userPhone.getId().equals(id)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该手机号已被用户：" + userPhone.getUsername() + "绑定");
            }
        }

    }

    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);

        return BeanCopyUtils.copyBean(user, UserVO.class);
    }
}
