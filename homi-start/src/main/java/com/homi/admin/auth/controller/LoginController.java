package com.homi.admin.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.homi.admin.auth.dto.login.LoginDTO;
import com.homi.admin.auth.dto.login.TokenRefreshDTO;
import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.LoginLog;
import com.homi.domain.RedisKey;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.menu.AsyncRoutesVO;
import com.homi.exception.BizException;
import com.homi.service.company.CompanyService;
import com.homi.service.company.CompanyUserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;

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

    private final StringRedisTemplate redisTemplate;

    private final CompanyService companyService;

    private final CompanyUserService companyUserService;

    @GetMapping("/admin/captcha/{username}")
    public void captcha(@PathVariable("username") Long username, HttpServletResponse response) throws IOException {
        // 生成验证码
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 80, randomGenerator, 100);
        String code = lineCaptcha.getCode();


        // 保存到 Redis，key: captcha:uuid，value: code，有效期10分钟
        redisTemplate.opsForValue().set(RedisKey.CAPTCHA.format(username), code, RedisKey.CAPTCHA.getTimeout(), RedisKey.CAPTCHA.getUnit());

        response.setContentType("image/png");

        ImageIO.write(lineCaptcha.getImage(), "png", response.getOutputStream());
    }

    @LoginLog
    @PostMapping("/admin/login")
    public ResponseResult<UserLoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserLoginVO userLogin = authService.checkUserLogin(loginDTO);

        return ResponseResult.ok(authService.login(userLogin));
    }

    @PostMapping("/admin/token/refresh")
    @LoginLog
    public ResponseResult<UserLoginVO> refresh(@RequestBody TokenRefreshDTO req) {
        return ResponseResult.ok(authService.refreshLogin(req.getRefreshToken()));
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

    @GetMapping("/admin/get-async-routes")
    public ResponseResult<List<AsyncRoutesVO>> getUserRoutes() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();

        return ResponseResult.ok(authService.getUserRoutes(currentUser));
    }

    /**
     * 切换公司
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/12 11:14
     *
     * @param companyId 参数说明
     * @return com.homi.domain.base.ResponseResult<com.homi.admin.auth.vo.login.UserLoginVO>
     */
    @PostMapping("/admin/switchCompany")
    public ResponseResult<UserLoginVO> switchCompany(@RequestParam("companyId") Long companyId) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        // 校验用户是否有这个公司权限
        if (!companyUserService.userHasCompany(currentUser.getId(), companyId)) {
            throw new BizException("无效的公司ID");
        }

        return ResponseResult.ok(authService.loginWithCompanyId(currentUser.getId(), companyId));
    }

}
