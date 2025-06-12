package com.homi.service.system;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.user.UserQueryDTO;
import com.homi.domain.enums.common.BizStatusEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.RoleDefaultEnum;
import com.homi.domain.vo.user.SysUserVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysUser;
import com.homi.model.entity.SysUserRole;
import com.homi.model.mapper.SysUserMapper;
import com.homi.model.mapper.SysUserRoleMapper;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public class SysUserService {
    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysConfigService sysConfigService;

    private final SysRoleService roleService;

    @Transactional(rollbackFor = Exception.class)
    public Long createUser(SysUser sysUser) {
        validateUserUniqueness(null, sysUser.getUsername(), sysUser.getEmail(), sysUser.getPhone());
        sysUser.setAvatar(sysConfigService.getConfigValueByKey(USER_DEFAULT_AVATAR));
        // 密码加密
        sysUser.setPassword(SaSecureUtil.md5(sysUser.getPassword()));
        sysUser.setCreateTime(DateUtil.date());
        sysUserMapper.insert(sysUser);

        userRoleMapper.insert(SysUserRole.builder().userId(sysUser.getId()).roleId(RoleDefaultEnum.USER.getId()).build());

        return sysUser.getId();
    }

    public Long updateUser(SysUser sysUser) {
        // 是否存在
        validateUserExists(sysUser.getId());
        // 有冻结的行为
        if (sysUser.getStatus().equals(BizStatusEnum.DISABLED.getValue())) {
            if (Long.valueOf(StpUtil.getLoginId().toString()).equals(sysUser.getId())) {
                throw new BizException("无法冻结自身");
            }
            List<Long> roleIdList = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, sysUser.getId())).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            if (roleService.hasSuperAdmin(roleIdList)) {
                throw new BizException("无法冻结超级管理员");
            }
        }
        // 邮箱这些是否有重复
        validateUserUniqueness(sysUser.getId(), sysUser.getUsername(), sysUser.getEmail(), sysUser.getPhone());

        sysUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysUserMapper.updateById(sysUser);
        return sysUser.getId();
    }

    public PageVO<SysUserVO> getUserList(UserQueryDTO query) {
        Page<SysUserVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        IPage<SysUserVO> sysUserVOPage = sysUserMapper.selectUserList(page, query);

        PageVO<SysUserVO> pageVO = new PageVO<>();
        pageVO.setTotal(sysUserVOPage.getTotal());
        pageVO.setList(sysUserVOPage.getRecords());
        pageVO.setCurrentPage(sysUserVOPage.getCurrent());
        pageVO.setPageSize(sysUserVOPage.getSize());
        pageVO.setPages(sysUserVOPage.getPages());

        return pageVO;
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
        SysUser user = sysUserMapper.selectById(id);
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

    public SysUserVO getUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);

        return BeanCopyUtils.copyBean(sysUser, SysUserVO.class);
    }
}
