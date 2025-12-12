package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.PlatformUserRole;
import com.homi.model.dao.mapper.PlatformUserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户和角色关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-22
 */
@Service
public class PlatformUserRoleRepo extends ServiceImpl<PlatformUserRoleMapper, PlatformUserRole> {

    /**
     * 根据用户 ID 获取角色列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/22 20:04
     *
     * @param userId 参数说明
     * @return java.util.List<com.nest.model.entity.PlatformUserRole>
     */
    public List<PlatformUserRole> getRoleListByUserId(Long userId) {
        LambdaQueryWrapper<PlatformUserRole> queryWrapper = new LambdaQueryWrapper<PlatformUserRole>()
            .eq(PlatformUserRole::getUserId, userId);
        return baseMapper.selectList(queryWrapper);
    }

    public void deleteUserRoleByUserId(Long userId) {
        LambdaQueryWrapper<PlatformUserRole> queryWrapper = new LambdaQueryWrapper<PlatformUserRole>()
            .eq(PlatformUserRole::getUserId, userId);

        remove(queryWrapper);
    }
}
