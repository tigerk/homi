package com.homi.admin.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homi.admin.auth.dto.UserLoginDTO;
import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.UserLoginVO;
import com.homi.annotation.LoginLog;
import com.homi.domain.base.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/17
 */

@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;

    @LoginLog
    @PostMapping("/admin/login")
    public ResponseResult<UserLoginVO> login(@Valid @RequestBody UserLoginDTO user) {
        return ResponseResult.ok(authService.login(user));
    }

    @PostMapping("/admin/logout")
    public ResponseResult<Void> logout() {
        StpUtil.getTokenSession().clear();
        StpUtil.logout();
        return ResponseResult.ok();
    }
}
