package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.User;
import com.homi.model.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class SysUserRepo extends ServiceImpl<SysUserMapper, User> {
    public User getUserByUsername(String username) {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

}
