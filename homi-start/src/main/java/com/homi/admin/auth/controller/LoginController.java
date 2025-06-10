package com.homi.admin.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homi.admin.auth.dto.login.UserLoginDTO;
import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.LoginLog;
import com.homi.domain.base.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class LoginController {

    private final AuthService authService;

    @LoginLog
    @PostMapping("/admin/login")
    public ResponseResult<UserLoginVO> login(@Valid @RequestBody UserLoginDTO user) {
        return ResponseResult.ok(authService.login(user));
    }

    @PostMapping("/admin/logout")
    public ResponseResult<Void> logout() {
        StpUtil.getSession().clear();
        StpUtil.logout();
        return ResponseResult.ok();
    }

    /**
     * 获取当前登录用户
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/9 13:48
     *
     * @return com.homi.domain.base.ResponseResult<com.homi.admin.auth.vo.login.UserLoginVO>
     */
    @LoginLog
    @PostMapping("/admin/login/current")
    public ResponseResult<UserLoginVO> getCurrentUser() {
        return ResponseResult.ok(LoginManager.getCurrentUser());
    }
}
