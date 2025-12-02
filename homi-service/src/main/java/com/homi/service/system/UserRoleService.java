package com.homi.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.entity.UserRole;
import com.homi.model.mapper.UserRoleMapper;
import com.homi.model.repo.UserRoleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleMapper userRoleMapper;

    private final UserRoleRepo userRoleRepo;


    public long getUserCountByRoleId(Long id) {
        return userRoleRepo.count(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
    }

    public void createUserRole(Long createdUserId, Long sysRoleId) {
        userRoleMapper.insert(UserRole.builder().roleId(sysRoleId).userId(createdUserId).build());
    }
}
