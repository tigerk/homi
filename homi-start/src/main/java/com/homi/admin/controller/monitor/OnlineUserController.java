package com.homi.admin.controller.monitor;


import cn.dev33.satoken.stp.StpUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.model.entity.SysLoginLog;
import com.homi.model.repo.SysLoginLogRepo;
import com.homi.model.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/monitor/online")
@RestController
@RequiredArgsConstructor
public class OnlineUserController {
    private final SysLoginLogRepo sysLoginLogRepo;
    private final UserRepo userRepo;

    @GetMapping("/users")
    public ResponseResult<List<SysLoginLog>> getOnlineUsers() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();

        // 获取所有登录的用户ids
        List<String> sessionIds = StpUtil.searchSessionId("", 0, -1, false);

        return ResponseResult.ok(sysLoginLogRepo.getLoginUsers(currentUser.getCompanyId(), sessionIds));
    }
}

