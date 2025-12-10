package com.homi.saas.web.controller.monitor;


import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.saas.web.auth.service.AuthService;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dao.entity.LoginLog;
import com.homi.model.dao.repo.LoginLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/saas/monitor/online")
@RestController
@RequiredArgsConstructor
public class OnlineUserController {
    private final LoginLogRepo loginLogRepo;

    private final AuthService authService;

    @GetMapping("/users")
    public ResponseResult<List<LoginLog>> getOnlineUsers() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();

        // 获取所有登录的用户ids
        List<String> sessionIds = StpUtil.searchSessionId("", 0, -1, false);

        List<String> validTokens = new ArrayList<>();
        for (String sessionId : sessionIds) {
            SaSession sessionBySessionId = StpUtil.getSessionBySessionId(sessionId);
            List<String> sessionTokens = StpUtil.getTokenValueListByLoginId(sessionBySessionId.getLoginId());

            validTokens.addAll(sessionTokens);
        }

        return ResponseResult.ok(loginLogRepo.getLoginUsers(currentUser.getCurCompanyId(), validTokens));
    }

    @PostMapping("/offline")
    public ResponseResult<Boolean> offlineUser(@RequestBody LoginLog loginLog) {
        return ResponseResult.ok(authService.kickUserByUsername(loginLog.getUsername()));
    }
}

