package com.homi.admin.controller.monitor;


import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.monitor.LoginLogDTO;
import com.homi.model.entity.SysLoginLog;
import com.homi.model.repo.SysLoginLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/monitor/login/log")
@RestController
@RequiredArgsConstructor
public class LoginLogController {
    private final SysLoginLogRepo sysLoginLogRepo;

    private final AuthService authService;

    @PostMapping("/list")
    public ResponseResult<PageVO<SysLoginLog>> getLoginLogList(@RequestBody LoginLogDTO dto) {
        return ResponseResult.ok(sysLoginLogRepo.getLoginLogList(dto));
    }


    @PostMapping("/clear/all")
    public ResponseResult<Boolean> clearAll() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        int deleted = sysLoginLogRepo.clearAllByCompanyId(currentUser.getCompanyId());

        return ResponseResult.ok(deleted > 0);
    }
}

