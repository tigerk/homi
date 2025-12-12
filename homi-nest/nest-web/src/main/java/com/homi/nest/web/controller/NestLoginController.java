package com.homi.nest.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homi.common.lib.annotation.LoginLog;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.nest.web.config.LoginManager;
import com.homi.nest.web.dto.login.TokenRefreshDTO;
import com.homi.nest.web.dto.login.UserLoginDTO;
import com.homi.nest.web.service.AuthService;
import com.homi.nest.web.vo.login.NestUserLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 应用于 nest-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/17
 */

@RequiredArgsConstructor
@Slf4j
@RestController
public class NestLoginController {

    private final AuthService authService;

    @LoginLog
    @PostMapping("/nest/login")
    public ResponseResult<NestUserLoginVO> login(@Valid @RequestBody UserLoginDTO user) {
        return ResponseResult.ok(authService.login(user));
    }

    @PostMapping("/nest/token/refresh")
    public ResponseResult<NestUserLoginVO> refresh(@RequestBody TokenRefreshDTO req) {
        Long userId = authService.getUserIdByToken(req.getRefreshToken());

        return ResponseResult.ok(authService.loginSession(userId));
    }

    @PostMapping("/nest/logout")
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
     * @return com.nest.domain.base.ResponseResult<com.nest.admin.auth.vo.login.NestUserLoginVO>
     */
    @LoginLog
    @PostMapping("/nest/login/current")
    public ResponseResult<NestUserLoginVO> getCurrentUser() {
        return ResponseResult.ok(LoginManager.getCurrentUser());
    }

    @GetMapping("/nest/get-async-routes")
    public ResponseResult<List<AsyncRoutesVO>> getUserRoutes() {
        return ResponseResult.ok(authService.getUserRoutes(LoginManager.getUserId()));
    }
}
