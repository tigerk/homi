package com.homi.saas.web.controller.sys;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.enums.sys.notice.SysNoticeTargetScopeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.notice.dto.*;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/sys/notice")
@Tag(name = "系统通知")
public class SysNoticeController {
    private final SysMessageRepo sysMessageRepo;
    private final SysNoticeRepo sysNoticeRepo;
    private final SysNoticeRoleRepo sysNoticeRoleRepo;
    private final SysNoticeReadRepo sysNoticeReadRepo;
    private final SysTodoRepo sysTodoRepo;
    private final CompanyUserRepo companyUserRepo;
    private final UserRepo userRepo;

    @PostMapping("/recent")
    @Operation(summary = "获取最近系统通知")
    public ResponseResult<RecentNoticeVO> getRecent(@RequestBody SysNoticeRecentDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        List<Long> roleIds = currentUser.getRoles() == null ? List.of() : currentUser.getRoles().stream().map(Long::valueOf).collect(Collectors.toList());

        // 默认获取三条数据
        int limit = 3;

        List<SysMessage> messages = sysMessageRepo.getRecentMessages(currentUser.getCurCompanyId(), currentUser.getId(), limit);
        List<SysNotice> notices = sysNoticeRepo.getRecentNotices(currentUser.getCurCompanyId(), limit, roleIds);
        markNoticeReadState(notices, currentUser.getId());
        List<SysTodo> todos = sysTodoRepo.getRecentTodos(currentUser.getCurCompanyId(), currentUser.getId(), limit);

        Long unreadMessageCount = sysMessageRepo.countUnreadMessages(currentUser.getCurCompanyId(), currentUser.getId());
        List<Long> noticeIds = sysNoticeRepo.listNoticeIdsForUser(currentUser.getCurCompanyId(), roleIds);
        Long readNoticeCount = sysNoticeReadRepo.countReadByUserAndNoticeIds(currentUser.getId(), noticeIds);
        Long unreadNoticeCount = Math.max(0L, noticeIds.size() - readNoticeCount);
        Long pendingTodoCount = sysTodoRepo.countPendingTodos(currentUser.getCurCompanyId(), currentUser.getId());

        RecentNoticeVO vo = new RecentNoticeVO();
        vo.setMessages(messages);
        vo.setNotices(notices);
        vo.setTodos(todos);
        vo.setUnreadMessageCount(unreadMessageCount);
        vo.setUnreadNoticeCount(unreadNoticeCount);
        vo.setPendingTodoCount(pendingTodoCount);

        return ResponseResult.ok(vo);
    }

    @PostMapping("/message/my/page")
    @Operation(summary = "获取个人消息分页")
    public ResponseResult<PageVO<SysMessage>> getMessagePage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(sysMessageRepo.getMyMessagePage(dto, currentUser.getCurCompanyId(), currentUser.getId()));
    }

    @PostMapping("/message/admin/page")
    @Operation(summary = "获取消息分页（管理端）")
    public ResponseResult<PageVO<SysMessage>> getMessageAdminPage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        PageVO<SysMessage> pageVO = sysMessageRepo.getMessagePageForAdmin(dto, currentUser.getCurCompanyId());
        fillMessageReceiverName(pageVO.getList());
        return ResponseResult.ok(pageVO);
    }

    @PostMapping("/notice/admin/page")
    @Operation(summary = "获取系统公告分页（管理端）")
    public ResponseResult<PageVO<SysNotice>> getNoticePage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        PageVO<SysNotice> pageVO = sysNoticeRepo.getNoticePageForAdmin(dto, currentUser.getCurCompanyId());
        return ResponseResult.ok(pageVO);
    }

    @PostMapping("/notice/my/page")
    @Operation(summary = "获取我的公告分页")
    public ResponseResult<PageVO<SysNotice>> getMyNoticePage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        List<Long> roleIds = currentUser.getRoles() == null ? List.of() : currentUser.getRoles().stream().map(Long::valueOf).collect(Collectors.toList());
        PageVO<SysNotice> pageVO = sysNoticeRepo.getMyNoticePage(dto, currentUser.getCurCompanyId(), roleIds);
        markNoticeReadState(pageVO.getList(), currentUser.getId());
        return ResponseResult.ok(pageVO);
    }

    @PostMapping("/todo/my/page")
    @Operation(summary = "获取待办消息分页")
    public ResponseResult<PageVO<SysTodo>> getTodoPage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(sysTodoRepo.getMyTodoPage(dto, currentUser.getCurCompanyId(), currentUser.getId()));
    }

    @PostMapping("/todo/admin/page")
    @Operation(summary = "获取待办分页（管理端）")
    public ResponseResult<PageVO<SysTodo>> getTodoAdminPage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        PageVO<SysTodo> pageVO = sysTodoRepo.getTodoPageForAdmin(dto, currentUser.getCurCompanyId());
        fillTodoExecutorName(pageVO.getList());
        return ResponseResult.ok(pageVO);
    }

    @PostMapping("/message/send")
    @Operation(summary = "发送站内信")
    @Log(title = "发送站内信", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Boolean> sendMessage(@RequestBody SysMessageSendDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        Long companyId = currentUser.getCurCompanyId();
        if (companyUserRepo.getCompanyUser(companyId, dto.getReceiverId()) == null) {
            throw new BizException(ResponseCodeEnum.NO_FOUND);
        }
        SysMessage message = new SysMessage();
        message.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId(message));
        message.setCompanyId(companyId);
        message.setSenderId(currentUser.getId());
        message.setReceiverId(dto.getReceiverId());
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setMsgType(dto.getMsgType());
        message.setIsRead(false);
        message.setDeletedBySender(false);
        message.setDeletedByReceiver(false);
        message.setCreateTime(DateUtil.date());
        return ResponseResult.ok(sysMessageRepo.save(message));
    }

    @PostMapping("/message/read")
    @Operation(summary = "标记消息已读")
    public ResponseResult<Boolean> markMessageRead(@RequestBody SysMessageReadDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        SysMessage message = sysMessageRepo.getById(dto.getId());
        if (message == null) {
            throw new BizException(ResponseCodeEnum.NO_FOUND);
        }
        if (!Objects.equals(message.getReceiverId(), currentUser.getId())) {
            throw new BizException(ResponseCodeEnum.AUTHORIZED);
        }
        message.setIsRead(true);
        message.setReadTime(DateUtil.date());
        return ResponseResult.ok(sysMessageRepo.updateById(message));
    }

    @PostMapping("/message/read/batch")
    @Operation(summary = "批量标记消息已读")
    public ResponseResult<Boolean> markMessageReadBatch(@RequestBody SysReadBatchDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return ResponseResult.ok(true);
        }
        Date now = DateUtil.date();
        boolean updated = sysMessageRepo.update(new LambdaUpdateWrapper<SysMessage>()
            .set(SysMessage::getIsRead, true)
            .set(SysMessage::getReadTime, now)
            .eq(SysMessage::getReceiverId, currentUser.getId())
            .in(SysMessage::getId, dto.getIds()));
        return ResponseResult.ok(updated);
    }

    @PostMapping("/notice/read")
    @Operation(summary = "标记公告已读")
    public ResponseResult<Boolean> markNoticeRead(@RequestBody SysNoticeReadDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        SysNotice notice = sysNoticeRepo.getById(dto.getId());
        if (notice == null) {
            throw new BizException(ResponseCodeEnum.NO_FOUND);
        }
        boolean exists = sysNoticeReadRepo.exists(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysNoticeRead>()
            .eq(SysNoticeRead::getUserId, currentUser.getId())
            .eq(SysNoticeRead::getNoticeId, dto.getId()));
        if (exists) {
            return ResponseResult.ok(true);
        }
        SysNoticeRead read = new SysNoticeRead();
        read.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId(read));
        read.setNoticeId(dto.getId());
        read.setUserId(currentUser.getId());
        read.setReadTime(DateUtil.date());
        sysNoticeReadRepo.save(read);
        return ResponseResult.ok(true);
    }

    @PostMapping("/notice/read/batch")
    @Operation(summary = "批量标记公告已读")
    public ResponseResult<Boolean> markNoticeReadBatch(@RequestBody SysReadBatchDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return ResponseResult.ok(true);
        }
        List<Long> existing = sysNoticeReadRepo.listNoticeIdsByUserIdAndNoticeIds(currentUser.getId(), dto.getIds());
        List<Long> toSave = dto.getIds().stream()
            .filter(id -> !existing.contains(id))
            .toList();
        if (toSave.isEmpty()) {
            return ResponseResult.ok(true);
        }
        Date now = DateUtil.date();
        List<SysNoticeRead> records = new ArrayList<>(toSave.size());
        toSave.forEach(id -> {
            SysNoticeRead read = new SysNoticeRead();
            read.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId(read));
            read.setNoticeId(id);
            read.setUserId(currentUser.getId());
            read.setReadTime(now);
            records.add(read);
        });
        sysNoticeReadRepo.saveBatch(records);
        return ResponseResult.ok(true);
    }

    @PostMapping("/todo/read")
    @Operation(summary = "标记待办已读")
    public ResponseResult<Boolean> markTodoRead(@RequestBody SysTodoReadDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        SysTodo todo = sysTodoRepo.getById(dto.getId());
        if (todo == null) {
            throw new BizException(ResponseCodeEnum.NO_FOUND);
        }
        if (!Objects.equals(todo.getUserId(), currentUser.getId())) {
            throw new BizException(ResponseCodeEnum.AUTHORIZED);
        }
        todo.setIsRead(true);
        todo.setReadTime(DateUtil.date());
        todo.setUpdateBy(currentUser.getId());
        todo.setUpdateTime(DateUtil.date());
        return ResponseResult.ok(sysTodoRepo.updateById(todo));
    }

    @PostMapping("/todo/read/batch")
    @Operation(summary = "批量标记待办已读")
    public ResponseResult<Boolean> markTodoReadBatch(@RequestBody SysReadBatchDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return ResponseResult.ok(true);
        }
        Date now = DateUtil.date();
        boolean updated = sysTodoRepo.update(new LambdaUpdateWrapper<SysTodo>()
            .set(SysTodo::getIsRead, true)
            .set(SysTodo::getReadTime, now)
            .set(SysTodo::getUpdateBy, currentUser.getId())
            .set(SysTodo::getUpdateTime, now)
            .eq(SysTodo::getUserId, currentUser.getId())
            .in(SysTodo::getId, dto.getIds()));
        return ResponseResult.ok(updated);
    }

    @PostMapping("/todo/handle")
    @Operation(summary = "标记待办已处理")
    public ResponseResult<Boolean> handleTodo(@RequestBody SysTodoHandleDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        SysTodo todo = sysTodoRepo.getById(dto.getId());
        if (todo == null) {
            throw new BizException(ResponseCodeEnum.NO_FOUND);
        }
        if (!Objects.equals(todo.getUserId(), currentUser.getId())) {
            throw new BizException(ResponseCodeEnum.AUTHORIZED);
        }
        Date now = DateUtil.date();
        todo.setStatus(1);
        todo.setHandleRemark(dto.getHandleRemark());
        todo.setHandleTime(now);
        todo.setUpdateBy(currentUser.getId());
        todo.setUpdateTime(now);
        return ResponseResult.ok(sysTodoRepo.updateById(todo));
    }

    @PostMapping("/create")
    @Operation(summary = "发布/修改系统公告")
    @Log(title = "发布/修改系统公告", operationType = OperationTypeEnum.UPDATE)
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
    @Log(title = "删除系统公告", operationType = OperationTypeEnum.DELETE)
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

    private void markNoticeReadState(List<SysNotice> notices, Long userId) {
        if (notices == null || notices.isEmpty()) {
            return;
        }
        List<Long> noticeIds = notices.stream()
            .map(SysNotice::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (noticeIds.isEmpty()) {
            return;
        }
        List<Long> readIds = sysNoticeReadRepo.listNoticeIdsByUserIdAndNoticeIds(userId, noticeIds);
        notices.forEach(item -> item.setIsRead(readIds.contains(item.getId())));
    }

    /**
     * 通用的用户名填充方法
     *
     * @param list            需要填充的列表
     * @param userIdExtractor 用户ID提取函数
     * @param nameSetter      用户名设置函数
     * @param <T>             列表元素类型
     */
    private <T> void fillUserName(List<T> list,
                                  java.util.function.Function<T, Long> userIdExtractor,
                                  java.util.function.BiConsumer<T, String> nameSetter) {
        if (list == null || list.isEmpty()) {
            return;
        }

        List<Long> userIds = list.stream()
            .map(userIdExtractor)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, String> nameMap = userRepo.listByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        list.forEach(item -> nameSetter.accept(item,
            nameMap.getOrDefault(userIdExtractor.apply(item), "")));
    }

    // 简化后的方法
    private void fillMessageReceiverName(List<SysMessage> list) {
        fillUserName(list, SysMessage::getReceiverId, SysMessage::setReceiverName);
    }

    private void fillTodoExecutorName(List<SysTodo> list) {
        fillUserName(list, SysTodo::getUserId, SysTodo::setExecutorName);
    }
}
