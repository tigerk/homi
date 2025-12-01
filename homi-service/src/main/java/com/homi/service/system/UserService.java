package com.homi.service.system;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.user.UserCreateDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.User;
import com.homi.model.mapper.UserMapper;
import com.homi.model.repo.CompanyUserRepo;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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

    private final SysConfigService sysConfigService;

    private final DeptService deptService;

    private final CompanyUserRepo companyUserRepo;

    @Value("${default-avatar}")
    private String defaultAvatar;

    /**
     * 创建用户
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/20 13:57
     *
     * @param createDTO 参数说明
     * @return java.lang.Long
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(@Valid UserCreateDTO createDTO) {
        User user = BeanCopyUtils.copyBean(createDTO, User.class);
        assert user != null;

        validateUserUniqueness(null, user.getUsername(), user.getEmail(), user.getPhone());
        user.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        user.setUpdateTime(DateUtil.date());

        if (Objects.nonNull(user.getPassword())) {
            // 密码加密
            user.setPassword(SaSecureUtil.md5(user.getPassword()));
        }

        user.setRealName(user.getRealName());
        user.setIdNo(user.getIdNo());
        user.setIdType(user.getIdType());

        user.setAvatar(sysConfigService.getConfigValueByKey(USER_DEFAULT_AVATAR));
        user.setCreateBy(createDTO.getUpdateBy());
        user.setUpdateBy(createDTO.getUpdateBy());
        userMapper.insert(user);

        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long updateUser(@Valid UserCreateDTO updateUser) {
        // 是否存在
        User user = validateUserExists(updateUser.getUserId());
        Objects.requireNonNull(user);

        // 邮箱这些是否有重复
        validateUserUniqueness(user.getId(), user.getUsername(), user.getEmail(), user.getPhone());

        BeanUtils.copyProperties(updateUser, user);
        user.setUpdateBy(updateUser.getUpdateBy());

        userMapper.updateById(user);
        return user.getId();
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
    public void validateUserUniqueness(Long id, String name, String email, String phone) {
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

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public List<User> getUserByIds(List<Long> ids) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, ids);
        return userMapper.selectList(queryWrapper);
    }

    public User getUserByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    public User getUserByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }
}
