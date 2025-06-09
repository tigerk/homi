package com.homi.admin.auth.controller;

import com.homi.admin.auth.service.AuthService;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.LoginLog;
import com.homi.domain.base.ResponseResult;
import com.homi.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/17
 */

@RequiredArgsConstructor
@Slf4j
@RestController
public class UserController {

    private final AuthService authService;

    @LoginLog
    @PostMapping("/admin/user/get")
    public ResponseResult<SysUser> getUser() {
        return ResponseResult.ok(LoginManager.getCurrentUser());
    }

}
