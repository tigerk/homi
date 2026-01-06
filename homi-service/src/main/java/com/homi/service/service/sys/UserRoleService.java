package com.homi.service.service.sys;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.utils.CollectionUtils;
import com.homi.model.dao.entity.UserRole;
import com.homi.model.dao.mapper.UserRoleMapper;
import com.homi.model.dao.repo.UserRoleRepo;
import com.homi.model.vo.company.user.UserVO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.homi.common.lib.utils.CollectionUtils.convertSet;

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


    public void createUserRole(Long createdUserId, Long sysRoleId) {
        userRoleMapper.insert(UserRole.builder().roleId(sysRoleId).userId(createdUserId).build());
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

    /**
     * 给用户分配角色
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/29 23:17
     *
     * @param userId  参数说明
     * @param roleIds 参数说明
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleByUserId(Long userId, Set<Long> roleIds) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleRepo.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)),
            UserRole::getRoleId);
        // 计算新增和删除的角色编号
        Set<Long> roleIdList = CollUtil.emptyIfNull(roleIds);
        Collection<Long> createRoleIds = CollUtil.subtract(roleIdList, dbRoleIds);
        Collection<Long> deleteRoleIds = CollUtil.subtract(dbRoleIds, roleIdList);

        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollUtil.isEmpty(createRoleIds)) {
            userRoleRepo.saveBatch(CollectionUtils.convertList(createRoleIds, roleId -> UserRole.builder().userId(userId).roleId(roleId).build()));
            dbRoleIds.addAll(createRoleIds);
        }
        if (!CollUtil.isEmpty(deleteRoleIds)) {
            userRoleRepo.remove(new LambdaQueryWrapper<UserRole>().
                eq(UserRole::getUserId, userId).
                in(UserRole::getRoleId, deleteRoleIds));
            dbRoleIds.removeAll(deleteRoleIds);
        }
    }


    /**
     * 获取用户角色ID列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/29 22:16
     *
     * @param userId 参数说明
     * @return java.util.List<java.lang.Long>
     */
    public List<Long> getRoleIdsByUser(Long userId) {
        List<UserRole> userRoles = userRoleRepo.list(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userId));
        return userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }
}
