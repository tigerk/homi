package com.homi.saas.web.controller.sys;

import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.sys.notice.SysNoticeTargetScopeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.SysMessage;
import com.homi.model.dao.entity.SysNotice;
import com.homi.model.dao.entity.SysNoticeRole;
import com.homi.model.dao.entity.SysTodo;
import com.homi.model.dao.repo.*;
import com.homi.model.notice.dto.SysNoticeCreateDTO;
import com.homi.model.notice.dto.SysNoticePageDTO;
import com.homi.model.notice.dto.SysNoticeRecentDTO;
import com.homi.model.notice.vo.RecentNoticeVO;
import com.homi.model.notice.vo.SysNoticeDetailVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/sys/notice")
@Tag(name = "系统通知")
public class SysNoticeController {
    private final SysMessageRepo sysMessageRepo;
    private final SysNoticeRepo sysNoticeRepo;
    private final SysNoticeRoleRepo sysNoticeRoleRepo;
    private final SysTodoRepo sysTodoRepo;
    private final CompanyUserRepo companyUserRepo;
    private final UserRepo userRepo;

    @PostMapping("/recent")
    @Operation(summary = "获取最近系统通知")
    public ResponseResult<RecentNoticeVO> getRecent(@RequestBody SysNoticeRecentDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        int days = dto.getDays() == null || dto.getDays() <= 0 ? 3 : dto.getDays();
        Date startTime = DateUtil.offsetDay(DateUtil.date(), -days);
        List<Long> roleIds = currentUser.getRoles().stream().map(Long::valueOf).collect(Collectors.toList());

        List<SysMessage> messages = sysMessageRepo.getRecentMessages(currentUser.getCurCompanyId(), currentUser.getId(), startTime);
        List<SysNotice> notices = sysNoticeRepo.getRecentNotices(currentUser.getCurCompanyId(), startTime, roleIds);
        List<SysTodo> todos = sysTodoRepo.getRecentTodos(currentUser.getCurCompanyId(), currentUser.getId(), startTime);

        return ResponseResult.ok(new RecentNoticeVO(messages, notices, todos));
    }

    @PostMapping("/message/page")
    @Operation(summary = "获取个人消息分页")
    public ResponseResult<PageVO<SysMessage>> getMessagePage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(sysMessageRepo.getMessagePage(dto, currentUser.getCurCompanyId(), currentUser.getId()));
    }

    @PostMapping("/notice/page")
    @Operation(summary = "获取系统公告分页")
    public ResponseResult<PageVO<SysNotice>> getNoticePage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        List<Long> roleIds = currentUser.getRoles().stream().map(Long::valueOf).collect(Collectors.toList());
        return ResponseResult.ok(sysNoticeRepo.getNoticePage(dto, currentUser.getCurCompanyId(), roleIds));
    }

    @PostMapping("/todo/page")
    @Operation(summary = "获取待办消息分页")
    public ResponseResult<PageVO<SysTodo>> getTodoPage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(sysTodoRepo.getTodoPage(dto, currentUser.getCurCompanyId(), currentUser.getId()));
    }

    @PostMapping("/create")
    @Operation(summary = "发布/修改系统公告")
    public ResponseResult<Boolean> createOrUpdate(@RequestBody SysNoticeCreateDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        Date now = DateUtil.date();
        Integer targetScope = dto.getTargetScope() == null ? SysNoticeTargetScopeEnum.ALL.getCode() : dto.getTargetScope();

        if (Objects.nonNull(dto.getId())) {
            SysNotice existing = sysNoticeRepo.getById(dto.getId());
            if (existing == null) {
                throw new BizException(ResponseCodeEnum.NO_FOUND);
            }
            existing.setTitle(dto.getTitle());
            existing.setContent(dto.getContent());
            existing.setNoticeType(dto.getNoticeType());
            existing.setTargetScope(targetScope);
            existing.setRemark(dto.getRemark());
            existing.setUpdateBy(currentUser.getId());
            existing.setUpdateTime(now);
            boolean updated = sysNoticeRepo.updateById(existing);
            syncNoticeRoles(existing.getId(), targetScope, dto.getRoleIds());
            return ResponseResult.ok(updated);
        } else {
            SysNotice notice = buildSysNotice(dto, currentUser, now, targetScope);
            boolean saved = sysNoticeRepo.save(notice);
            if (saved) {
                syncNoticeRoles(notice.getId(), targetScope, dto.getRoleIds());
            }
            return ResponseResult.ok(saved);
        }
    }

    /**
     * 创建系统公告
     */
    private static SysNotice buildSysNotice(SysNoticeCreateDTO dto, UserLoginVO currentUser, Date now, Integer targetScope) {
        SysNotice notice = new SysNotice();
        notice.setCompanyId(currentUser.getCurCompanyId());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setNoticeType(dto.getNoticeType());
        notice.setTargetScope(targetScope);
        notice.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        notice.setPublishTime(dto.getPublishTime() == null ? now : dto.getPublishTime());
        notice.setRemark(dto.getRemark());
        notice.setCreateBy(currentUser.getId());
        notice.setCreateTime(now);
        notice.setUpdateBy(currentUser.getId());
        notice.setUpdateTime(now);
        return notice;
    }

    @PostMapping("/detail")
    @Operation(summary = "获取系统公告详情")
    public ResponseResult<SysNoticeDetailVO> detail(@RequestBody SysNoticeCreateDTO dto) {
        if (dto.getId() == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR);
        }
        SysNotice notice = sysNoticeRepo.getById(dto.getId());
        if (notice == null) {
            throw new BizException(ResponseCodeEnum.NO_FOUND);
        }
        notice.setCreateByName(userRepo.getUserNicknameById(notice.getCreateBy()));
        List<Long> roleIds = sysNoticeRoleRepo.listByNoticeId(dto.getId()).stream()
            .map(SysNoticeRole::getRoleId)
            .collect(Collectors.toList());
        return ResponseResult.ok(new SysNoticeDetailVO(notice, roleIds));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除系统公告")
    public ResponseResult<Boolean> delete(@RequestBody SysNoticeCreateDTO dto) {
        if (dto.getId() == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR);
        }
        sysNoticeRoleRepo.deleteByNoticeId(dto.getId());
        return ResponseResult.ok(sysNoticeRepo.removeById(dto.getId()));
    }

    private void syncNoticeRoles(Long noticeId, Integer targetScope, List<Long> roleIds) {
        sysNoticeRoleRepo.deleteByNoticeId(noticeId);
        if (!Objects.equals(targetScope, SysNoticeTargetScopeEnum.SPECIFIED_ROLE.getCode())) {
            return;
        }
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        roleIds.forEach(roleId -> {
            SysNoticeRole role = new SysNoticeRole();
            role.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId(role));
            role.setNoticeId(noticeId);
            role.setRoleId(roleId);
            sysNoticeRoleRepo.save(role);
        });
    }
}
