package com.homi.service.service.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.dao.entity.UserRole;
import com.homi.model.dao.mapper.UserRoleMapper;
import com.homi.model.dao.repo.UserRoleRepo;
import com.homi.model.vo.company.user.UserVO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 查询角色绑定的用户数量
     *
     * @param id 角色ID
     * @return 用户数量
     */
    public long getUserCountByRoleId(Long id) {
        return userRoleRepo.count(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
    }

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    public List<UserVO> getUserIdsByRoleId(@NotNull(message = "id不能为空") Long roleId) {
        return userRoleRepo.getBaseMapper().getUserListByRoleId(roleId);
    }
}
