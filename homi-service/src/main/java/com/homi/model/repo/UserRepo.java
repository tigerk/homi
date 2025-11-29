package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.User;
import com.homi.model.mapper.UserMapper;
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
