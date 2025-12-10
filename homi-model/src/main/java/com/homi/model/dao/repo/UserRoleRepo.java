package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.UserRole;
import com.homi.model.dao.mapper.UserRoleMapper;
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
