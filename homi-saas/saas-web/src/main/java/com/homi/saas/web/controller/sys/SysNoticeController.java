package com.homi.saas.web.controller.sys;

import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.SysMessage;
import com.homi.model.dao.entity.SysNotice;
import com.homi.model.dao.entity.SysTodo;
import com.homi.model.dao.repo.SysMessageRepo;
import com.homi.model.dao.repo.SysNoticeRepo;
import com.homi.model.dao.repo.SysTodoRepo;
import com.homi.model.notice.dto.SysNoticePageDTO;
import com.homi.model.notice.dto.SysNoticeRecentDTO;
import com.homi.model.notice.vo.RecentNoticeVO;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/sys/notice")
@Tag(name = "系统通知")
public class SysNoticeController {
    private final SysMessageRepo sysMessageRepo;
    private final SysNoticeRepo sysNoticeRepo;
    private final SysTodoRepo sysTodoRepo;

    @PostMapping("/recent")
    @Operation(summary = "获取最近系统通知")
    public ResponseResult<RecentNoticeVO> getRecent(@RequestBody SysNoticeRecentDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        int days = dto.getDays() == null || dto.getDays() <= 0 ? 3 : dto.getDays();
        Date startTime = DateUtil.offsetDay(DateUtil.date(), -days);

        List<SysMessage> messages = sysMessageRepo.getRecentMessages(currentUser.getCurCompanyId(), currentUser.getId(), startTime);
        List<SysNotice> notices = sysNoticeRepo.getRecentNotices(currentUser.getCurCompanyId(), startTime);
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
        return ResponseResult.ok(sysNoticeRepo.getNoticePage(dto, currentUser.getCurCompanyId()));
    }

    @PostMapping("/todo/page")
    @Operation(summary = "获取待办消息分页")
    public ResponseResult<PageVO<SysTodo>> getTodoPage(@RequestBody SysNoticePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(sysTodoRepo.getTodoPage(dto, currentUser.getCurCompanyId(), currentUser.getId()));
    }
}
