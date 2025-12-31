package com.homi.platform.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homi.common.lib.annotation.LoginLog;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.dto.login.TokenRefreshDTO;
import com.homi.platform.web.dto.login.PlatformLoginDTO;
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

    /**
     * 登录
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/17 14:23
     *
     * @param user 用户登录信息
     * @return com.nest.domain.base.ResponseResult<com.nest.admin.auth.vo.login.PlatformUserLoginVO>
     */
    @LoginLog
    @PostMapping("/login")
    public ResponseResult<PlatformUserLoginVO> login(@Valid @RequestBody PlatformLoginDTO user) {
        return ResponseResult.ok(platformAuthService.login(user));
    }

    @PostMapping("/login/user/get")
    public ResponseResult<PlatformUserLoginVO> getLoginUser() {
        return ResponseResult.ok(PlatformLoginManager.getCurrentUser());
    }

    @PostMapping("/token/refresh")
    public ResponseResult<PlatformUserLoginVO> refresh(@RequestBody TokenRefreshDTO req) {
        Long userId = (Long) StpUtil.getLoginIdByToken(req.getRefreshToken());

        // 校验用户是否存在
        if (userId == null) {
            throw new BizException(ResponseCodeEnum.TOKEN_ERROR);
        }

        return ResponseResult.ok(PlatformLoginManager.getCurrentUser());
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
