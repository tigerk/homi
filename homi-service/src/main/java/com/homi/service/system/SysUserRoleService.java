package com.homi.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.entity.SysUserRole;
import com.homi.model.mapper.SysUserRoleMapper;
import com.homi.model.repo.SysUserRoleRepo;
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
public class SysUserRoleService {
    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysUserRoleRepo userRoleRepo;


    public long getUserCountByRoleId(Long id) {
        return userRoleRepo.count(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
    }

    public void createUserRole(Long createdUserId, Long sysRoleId) {
        sysUserRoleMapper.insert(SysUserRole.builder().roleId(sysRoleId).userId(createdUserId).build());
    }
}
