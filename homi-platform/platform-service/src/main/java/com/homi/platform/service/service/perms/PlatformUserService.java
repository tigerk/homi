package com.homi.platform.service.service.perms;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.platform.PlatformUserTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.PasswordUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.PlatformUser;
import com.homi.model.dao.entity.PlatformUserRole;
import com.homi.model.dao.mapper.PlatformUserMapper;
import com.homi.model.dao.repo.PlatformUserRepo;
import com.homi.model.dao.repo.PlatformUserRoleRepo;
import com.homi.model.user.UserQueryDTO;
import com.homi.model.platform.dto.PlatformUserRoleAssignDTO;
import com.homi.model.platform.vo.PlatformUserVO;
import com.homi.model.common.vo.IdNameVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 应用于 nest-boot
 *
 * @author tk
 * @version v1.0 {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class PlatformUserService {
    private final PlatformUserMapper platformUserMapper;

    private final PlatformUserRepo platformUserRepo;
    private final PlatformUserRoleRepo platformUserRoleRepo;

    @Value("${default-avatar}")
    private String defaultAvatar;

    @Transactional(rollbackFor = Exception.class)
    public Long createUser(PlatformUser platformUser) {
        validateUserUniqueness(null, platformUser.getUsername(), platformUser.getEmail(), platformUser.getPhone());
        // 密码加密
        platformUser.setPassword(PasswordUtils.encryptPassword(platformUser.getPassword()));

        if (Objects.isNull(platformUser.getAvatar())) {
            platformUser.setAvatar(defaultAvatar);
        }

        platformUserMapper.insert(platformUser);

        return platformUser.getId();
    }

    public Long updateUser(PlatformUser platformUser) {
        // 是否存在
        validateUserExists(platformUser.getId());
        // 有冻结的行为
        if (platformUser.getStatus().equals(StatusEnum.DISABLED.getValue()) && Long.valueOf(StpUtil.getLoginId().toString()).equals(platformUser.getId())) {
            throw new BizException("无法冻结自身");
        }

        // 邮箱这些是否有重复
        validateUserUniqueness(platformUser.getId(), platformUser.getUsername(), platformUser.getEmail(), platformUser.getPhone());

        platformUser.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        platformUser.setUpdateTime(DateUtil.date());

        platformUserMapper.updateById(platformUser);
        return platformUser.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deleteByIds(List<Long> idList) {
        idList.forEach(this::deleteUserById);

        return idList.size();
    }

    public void deleteUserById(Long id) {
        platformUserRepo.removeById(id);
        platformUserRoleRepo.deleteUserRoleByUserId(id);
    }

    public void resetPassword(PlatformUser sysPlatformUser) {
        PlatformUser platformUser = validateUserExists(sysPlatformUser.getId());
        if (Objects.isNull(platformUser)) {
            throw new BizException("用户不存在");
        }

        platformUser.setPassword(SaSecureUtil.md5(sysPlatformUser.getPassword()));
        platformUserMapper.updateById(platformUser);
    }

    private PlatformUser validateUserExists(Long id) {
        if (id == null) {
            return null;
        }
        PlatformUser platformUser = platformUserMapper.selectById(id);
        if (Objects.isNull(platformUser)) {
            throw new BizException("用户不存在");
        }
        return platformUser;
    }

    /**
     * 校验用户名的唯一性
     *
     * @param id   用户ID，用于排除自身
     * @param name 用户名
     */
    private void validateUserUniqueness(Long id, String name, String email, String phone) {
        PlatformUser platformUserName = platformUserMapper.selectOne(new LambdaQueryWrapper<PlatformUser>().eq(PlatformUser::getUsername, name));
        if (platformUserName != null && !platformUserName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该用户名已存在");
        }
        if (CharSequenceUtil.isNotBlank(email)) {
            PlatformUser platformUserEmail = platformUserMapper.selectOne(new LambdaQueryWrapper<PlatformUser>().eq(PlatformUser::getEmail, email));
            if (platformUserEmail != null && !platformUserEmail.getId().equals(id)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该邮箱已被用户：" + platformUserEmail.getUsername() + "绑定");
            }
        }
        if (CharSequenceUtil.isNotBlank((phone))) {
            PlatformUser platformUserPhone = platformUserMapper.selectOne(new LambdaQueryWrapper<PlatformUser>().eq(PlatformUser::getPhone, phone));
            if (platformUserPhone != null && !platformUserPhone.getId().equals(id)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该手机号已被用户：" + platformUserPhone.getUsername() + "绑定");
            }
        }

    }

    public PlatformUser getUserById(Long id) {
        return platformUserMapper.selectById(id);
    }

    public void updateUserStatusByCompanyId(Long companyId, int status) {
        LambdaQueryWrapper<PlatformUser> queryWrapper = new LambdaQueryWrapper<>();

        PlatformUser updatePlatformUser = new PlatformUser();
        updatePlatformUser.setStatus(status);
        platformUserMapper.update(updatePlatformUser, queryWrapper);
    }

    /**
     * 获取公司的人员
     * <p>
     * {@code @author} tk {@code @date} 2025/6/26 09:31
     *
     * @param companyId 参数说明
     * @param type      参数说明
     * @return java.util.List<com.nest.model.entity.PlatformUser>
     */
    public List<PlatformUser> getCompanyUserByType(Long companyId, Integer type) {
        LambdaQueryWrapper<PlatformUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformUser::getUserType, type);

        return platformUserMapper.selectList(queryWrapper);
    }

    public PageVO<PlatformUserVO> getUserList(UserQueryDTO query) {
        Page<PlatformUser> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<PlatformUser> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(query.getUsername())) {
            queryWrapper.like(PlatformUser::getUsername, "%" + query.getUsername() + "%");
        }

        if (CharSequenceUtil.isNotBlank(query.getPhone())) {
            queryWrapper.eq(PlatformUser::getPhone, query.getPhone());
        }

        if (Objects.nonNull(query.getStatus())) {
            queryWrapper.eq(PlatformUser::getStatus, query.getStatus());
        }

        queryWrapper.orderByDesc(PlatformUser::getCreateTime);

        IPage<PlatformUser> userVOPage = platformUserRepo.page(page, queryWrapper);

        List<PlatformUserVO> records = userVOPage.getRecords().stream().map(user -> BeanCopyUtils.copyBean(user, PlatformUserVO.class)).collect(Collectors.toList());

        PageVO<PlatformUserVO> pageVO = new PageVO<>();
        pageVO.setTotal(userVOPage.getTotal());
        pageVO.setList(records);
        pageVO.setCurrentPage(userVOPage.getCurrent());
        pageVO.setPageSize(userVOPage.getSize());
        pageVO.setPages(userVOPage.getPages());

        return pageVO;
    }

    public void updateAvatar(PlatformUser updatePlatformUser) {
        PlatformUser platformUser = validateUserExists(updatePlatformUser.getId());
        if (Objects.isNull(platformUser)) {
            throw new BizException("用户不存在");
        }

        platformUser.setAvatar(updatePlatformUser.getAvatar());

        platformUserMapper.updateById(platformUser);
    }

    public List<IdNameVO> getUserListSimple() {
        LambdaQueryWrapper<PlatformUser> queryWrapper = new LambdaQueryWrapper<PlatformUser>()
            .eq(PlatformUser::getUserType, PlatformUserTypeEnum.REGULAR_USER.getType());

        return platformUserRepo.list(queryWrapper).stream()
            .map(user -> new IdNameVO(user.getId(), String.format("%s（%s）", user.getUsername(), user.getNickname())))
            .toList();
    }

    public List<Long> listRoleIds(Long userId) {
        return platformUserRoleRepo.list(new LambdaQueryWrapper<PlatformUserRole>()
                .eq(PlatformUserRole::getUserId, userId)).stream()
            .map(PlatformUserRole::getRoleId)
            .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserRole(@Valid PlatformUserRoleAssignDTO userRoleAssignDTO) {
        List<PlatformUserRole> platformUserRoles = platformUserRoleRepo.getRoleListByUserId(userRoleAssignDTO.getUserId());
        if (!platformUserRoles.isEmpty()) {
            platformUserRoleRepo.remove(new LambdaQueryWrapper<PlatformUserRole>().eq(PlatformUserRole::getUserId, userRoleAssignDTO.getUserId()));
        }

        return platformUserRoleRepo.saveOrUpdateBatch(userRoleAssignDTO.getRoleIds().stream()
            .map(roleId -> {
                PlatformUserRole platformUserRole = new PlatformUserRole();
                platformUserRole.setUserId(userRoleAssignDTO.getUserId());
                platformUserRole.setRoleId(roleId);
                return platformUserRole;
            })
            .collect(Collectors.toList()));
    }
}
