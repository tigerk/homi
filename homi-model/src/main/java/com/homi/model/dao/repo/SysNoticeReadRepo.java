package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.SysNoticeRead;
import com.homi.model.dao.mapper.SysNoticeReadMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 公告已读记录表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class SysNoticeReadRepo extends ServiceImpl<SysNoticeReadMapper, SysNoticeRead> {

    public List<Long> listNoticeIdsByUserId(Long userId) {
        return list(new LambdaQueryWrapper<SysNoticeRead>().eq(SysNoticeRead::getUserId, userId))
            .stream()
            .map(SysNoticeRead::getNoticeId)
            .distinct()
            .collect(Collectors.toList());
    }

    public List<Long> listNoticeIdsByUserIdAndNoticeIds(Long userId, List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<SysNoticeRead>()
            .eq(SysNoticeRead::getUserId, userId)
            .in(SysNoticeRead::getNoticeId, noticeIds))
            .stream()
            .map(SysNoticeRead::getNoticeId)
            .distinct()
            .collect(Collectors.toList());
    }

    public Long countReadByUserAndNoticeIds(Long userId, List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return 0L;
        }
        return count(new LambdaQueryWrapper<SysNoticeRead>()
            .eq(SysNoticeRead::getUserId, userId)
            .in(SysNoticeRead::getNoticeId, noticeIds));
    }
}
