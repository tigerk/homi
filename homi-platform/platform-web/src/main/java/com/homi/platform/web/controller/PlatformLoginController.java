package com.homi.platform.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homi.common.lib.annotation.LoginLog;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.dto.login.TokenRefreshDTO;
import com.homi.platform.web.dto.login.UserLoginDTO;
import com.homi.platform.web.service.PlatformAuthService;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/platform")
public class PlatformLoginController {

    private final PlatformAuthService platformAuthService;

    @LoginLog
    @PostMapping("/login")
    public ResponseResult<PlatformUserLoginVO> login(@Valid @RequestBody UserLoginDTO user) {
        return ResponseResult.ok(platformAuthService.login(user));
    }

    @PostMapping("/token/refresh")
    public ResponseResult<PlatformUserLoginVO> refresh(@RequestBody TokenRefreshDTO req) {
        Long userId = platformAuthService.getUserIdByToken(req.getRefreshToken());

        return ResponseResult.ok(platformAuthService.loginSession(userId));
    }

    @PostMapping("/logout")
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
     * @return com.nest.domain.base.ResponseResult<com.nest.admin.auth.vo.login.PlatformUserLoginVO>
     */
    @LoginLog
    @PostMapping("/login/current")
    public ResponseResult<PlatformUserLoginVO> getCurrentUser() {
        return ResponseResult.ok(PlatformLoginManager.getCurrentUser());
    }

    @GetMapping("/get-async-routes")
    public ResponseResult<List<AsyncRoutesVO>> getUserRoutes() {
        return ResponseResult.ok(platformAuthService.getUserRoutes(PlatformLoginManager.getUserId()));
    }
}
