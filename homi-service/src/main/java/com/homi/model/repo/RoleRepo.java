package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.Role;
import com.homi.model.mapper.RoleMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class RoleRepo extends ServiceImpl<RoleMapper, Role> {

}
