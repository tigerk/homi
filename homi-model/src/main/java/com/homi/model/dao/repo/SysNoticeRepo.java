package com.homi.model.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.sys.notice.SysNoticeTargetScopeEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.SysNotice;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.mapper.SysNoticeMapper;
import com.homi.model.notice.dto.SysNoticePageDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统公告表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class SysNoticeRepo extends ServiceImpl<SysNoticeMapper, SysNotice> {

    @Resource
    private UserRepo userRepo;
    @Resource
    private SysNoticeRoleRepo sysNoticeRoleRepo;

    public PageVO<SysNotice> getNoticePage(SysNoticePageDTO dto, Long companyId, List<Long> roleIds) {
        Page<SysNotice> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getCompanyId, companyId).eq(SysNotice::getStatus, StatusEnum.ACTIVE.getValue());
        applyRoleScopeFilter(wrapper, roleIds);

        if (CharSequenceUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(SysNotice::getTitle, dto.getKeyword())
                .or()
                .like(SysNotice::getContent, dto.getKeyword()));
        }

        wrapper.orderByDesc(SysNotice::getPublishTime);
        IPage<SysNotice> pageResult = getBaseMapper().selectPage(page, wrapper);

        List<SysNotice> records = pageResult.getRecords();
        fillCreateByName(records);

        PageVO<SysNotice> pageVO = new PageVO<>();
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setList(records);
        pageVO.setCurrentPage(pageResult.getCurrent());
        pageVO.setPageSize(pageResult.getSize());
        pageVO.setPages(pageResult.getPages());

        return pageVO;
    }

    public List<SysNotice> getRecentNotices(Long companyId, Date startTime, List<Long> roleIds) {
        Page<SysNotice> page = new Page<>(1, 10); // 第1页，每页10条

        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getCompanyId, companyId)
            .eq(SysNotice::getStatus, StatusEnum.ACTIVE.getValue())
            .notIn(SysNotice::getTargetScope, SysNoticeTargetScopeEnum.LANDLORD.getCode(), SysNoticeTargetScopeEnum.TENANT.getCode())
            .orderByDesc(SysNotice::getPublishTime);
        applyRoleScopeFilter(wrapper, roleIds);
        List<SysNotice> list = page(page, wrapper).getRecords();

        fillCreateByName(list);
        return list;
    }

    public List<Long> listNoticeIdsForUser(Long companyId, List<Long> roleIds) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysNotice::getId);
        wrapper.eq(SysNotice::getCompanyId, companyId)
            .eq(SysNotice::getStatus, StatusEnum.ACTIVE.getValue());
        applyRoleScopeFilter(wrapper, roleIds);
        return list(wrapper).stream()
            .map(SysNotice::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public PageVO<SysNotice> getMyNoticePage(SysNoticePageDTO dto, Long companyId, Long userId) {
        Page<SysNotice> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getCompanyId, companyId)
            .eq(SysNotice::getCreateBy, userId);

        if (CharSequenceUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(SysNotice::getTitle, dto.getKeyword())
                .or()
                .like(SysNotice::getContent, dto.getKeyword()));
        }

        wrapper.orderByDesc(SysNotice::getPublishTime);
        IPage<SysNotice> pageResult = getBaseMapper().selectPage(page, wrapper);
        List<SysNotice> records = pageResult.getRecords();
        fillCreateByName(records);

        PageVO<SysNotice> pageVO = new PageVO<>();
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setList(records);
        pageVO.setCurrentPage(pageResult.getCurrent());
        pageVO.setPageSize(pageResult.getSize());
        pageVO.setPages(pageResult.getPages());
        return pageVO;
    }

    private void applyRoleScopeFilter(LambdaQueryWrapper<SysNotice> wrapper, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            wrapper.ne(SysNotice::getTargetScope, SysNoticeTargetScopeEnum.SPECIFIED_ROLE.getCode());
            return;
        }
        List<Long> noticeIds = sysNoticeRoleRepo.listNoticeIdsByRoleIds(roleIds);
        if (noticeIds.isEmpty()) {
            wrapper.ne(SysNotice::getTargetScope, SysNoticeTargetScopeEnum.SPECIFIED_ROLE.getCode());
            return;
        }
        wrapper.and(w -> w.ne(SysNotice::getTargetScope, SysNoticeTargetScopeEnum.SPECIFIED_ROLE.getCode())
            .or()
            .in(SysNotice::getId, noticeIds));
    }

    private void fillCreateByName(List<SysNotice> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = list.stream()
            .map(SysNotice::getCreateBy)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }
        List<User> users = userRepo.listByIds(userIds);
        Map<Long, String> nameMap = users == null ? Collections.emptyMap() : users.stream()
            .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
        list.forEach(item -> item.setCreateByName(nameMap.getOrDefault(item.getCreateBy(), "")));
    }
}
