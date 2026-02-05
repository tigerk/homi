package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.SysNoticeRole;
import com.homi.model.dao.mapper.SysNoticeRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 公告角色关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class SysNoticeRoleRepo extends ServiceImpl<SysNoticeRoleMapper, SysNoticeRole> {

    public List<SysNoticeRole> listByNoticeId(Long noticeId) {
        return list(new LambdaQueryWrapper<SysNoticeRole>().eq(SysNoticeRole::getNoticeId, noticeId));
    }

    public boolean deleteByNoticeId(Long noticeId) {
        return remove(new LambdaQueryWrapper<SysNoticeRole>().eq(SysNoticeRole::getNoticeId, noticeId));
    }

    public List<Long> listNoticeIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<SysNoticeRole>().in(SysNoticeRole::getRoleId, roleIds))
            .stream()
            .map(SysNoticeRole::getNoticeId)
            .distinct()
            .collect(Collectors.toList());
    }
}
