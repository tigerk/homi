package com.homi.service.system;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.user.UserCreateDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.SysUser;
import com.homi.model.mapper.SysUserMapper;
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
    private final SysUserMapper sysUserMapper;

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
        SysUser sysUser = BeanCopyUtils.copyBean(createDTO, SysUser.class);
        assert sysUser != null;

        validateUserUniqueness(null, sysUser.getUsername(), sysUser.getEmail(), sysUser.getPhone());
        sysUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysUser.setUpdateTime(DateUtil.date());

        if (Objects.nonNull(sysUser.getPassword())) {
            // 密码加密
            sysUser.setPassword(SaSecureUtil.md5(sysUser.getPassword()));
        }

        sysUser.setRealName(sysUser.getRealName());
        sysUser.setIdNo(sysUser.getIdNo());
        sysUser.setIdType(sysUser.getIdType());

        sysUser.setAvatar(sysConfigService.getConfigValueByKey(USER_DEFAULT_AVATAR));
        sysUser.setCreateBy(createDTO.getUpdateBy());
        sysUser.setUpdateBy(createDTO.getUpdateBy());
        sysUserMapper.insert(sysUser);

        return sysUser.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long updateUser(@Valid UserCreateDTO updateUser) {
        // 是否存在
        SysUser sysUser = validateUserExists(updateUser.getUserId());
        Objects.requireNonNull(sysUser);

        // 邮箱这些是否有重复
        validateUserUniqueness(sysUser.getId(), sysUser.getUsername(), sysUser.getEmail(), sysUser.getPhone());

        BeanUtils.copyProperties(updateUser, sysUser);
        sysUser.setUpdateBy(updateUser.getUpdateBy());

        sysUserMapper.updateById(sysUser);
        return sysUser.getId();
    }

    public Integer deleteByIds(List<Long> idList) {
        Long loginId = Long.valueOf(StpUtil.getLoginId().toString());
        for (Long id : idList) {
            if (id.equals(loginId)) {
                throw new BizException("无法删除自身");
            }
        }
        sysUserMapper.deleteBatchIds(idList);
        return idList.size();
    }

    public void resetPassword(SysUser sysUser) {
        if (Objects.isNull(sysUser.getId()) || Objects.isNull(sysUser.getPassword())) {
            throw new BizException("参数异常");
        }

        SysUser user = validateUserExists(sysUser.getId());
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        user.setPassword(SaSecureUtil.md5(sysUser.getPassword()));
        sysUserMapper.updateById(user);
    }

    private SysUser validateUserExists(Long id) {
        if (id == null) {
            return null;
        }
        SysUser sysUser = sysUserMapper.selectById(id);
        if (Objects.isNull(sysUser)) {
            throw new BizException("用户不存在");
        }
        return sysUser;
    }

    /**
     * 校验用户名的唯一性
     *
     * @param id   用户ID，用于排除自身
     * @param name 用户名
     */
    public void validateUserUniqueness(Long id, String name, String email, String phone) {
        SysUser sysUserName = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, name));
        if (sysUserName != null && !sysUserName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该用户名已存在");
        }
        if (CharSequenceUtil.isNotBlank(email)) {
            SysUser sysUserEmail = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
            if (sysUserEmail != null && !sysUserEmail.getId().equals(id)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该邮箱已被用户：" + sysUserEmail.getUsername() + "绑定");
            }
        }
        if (CharSequenceUtil.isNotBlank((phone))) {
            SysUser sysUserPhone = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
            if (sysUserPhone != null && !sysUserPhone.getId().equals(id)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该手机号已被用户：" + sysUserPhone.getUsername() + "绑定");
            }
        }

    }

    public SysUser getUserById(Long id) {
        return sysUserMapper.selectById(id);
    }

    public List<SysUser> getUserByIds(List<Long> ids) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUser::getId, ids);
        return sysUserMapper.selectList(queryWrapper);
    }

    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public SysUser getUserByPhone(String phone) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
    }
}
