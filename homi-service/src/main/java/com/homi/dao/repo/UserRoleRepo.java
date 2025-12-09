package com.homi.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.dao.entity.UserRole;
import com.homi.dao.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户和角色关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class UserRoleRepo extends ServiceImpl<UserRoleMapper, UserRole> {

}
