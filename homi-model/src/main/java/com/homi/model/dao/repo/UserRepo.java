package com.homi.model.dao.repo;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.utils.PasswordUtils;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.mapper.UserMapper;
import com.homi.model.dto.company.CompanyCreateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class UserRepo extends ServiceImpl<UserMapper, User> {
    @Value("${default-avatar}")
    private String defaultAvatar;

    private static final String DEFAULT_PASSWORD = "house2026";

    public Pair<Long, ResponseCodeEnum> createCompanyAdmin(CompanyCreateDTO createDTO) {
        String phone = createDTO.getAdminPhone();

        User user = getBaseMapper().selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, phone));
        if (Objects.nonNull(user)) {
            return Pair.of(user.getId(), ResponseCodeEnum.ADMIN_EXIST);
        }

        user = new User();
        user.setUsername(phone);
        user.setNickname(DesensitizedUtil.mobilePhone(phone));
        user.setPhone(phone);
        user.setAvatar(defaultAvatar);
        user.setStatus(StatusEnum.ACTIVE.getValue());

        if (CharSequenceUtil.isBlank(createDTO.getAdminPassword())) {
            // 默认
            createDTO.setAdminPassword(DEFAULT_PASSWORD);
        }

        user.setPassword(PasswordUtils.encryptPassword(createDTO.getAdminPassword()));
        user.setCreateBy(createDTO.getCreateBy());
        user.setCreateTime(createDTO.getCreateTime());
        user.setUpdateBy(createDTO.getUpdateBy());
        user.setUpdateTime(createDTO.getUpdateTime());
        getBaseMapper().insert(user);
        return Pair.of(user.getId(), ResponseCodeEnum.SUCCESS);
    }

    public User getUserByUsername(String username) {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    /**
     * 根据用户名或手机号查询用户
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/29 12:12
     *
     * @param username 参数说明
     * @param phone    参数说明
     * @return com.homi.model.entity.User
     */
    public User getUserByUserNameOrPhone(String username, String phone) {
        // 校验用户是否存在
        return getBaseMapper().selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)
            .or().eq(User::getPhone, phone));
    }
}
