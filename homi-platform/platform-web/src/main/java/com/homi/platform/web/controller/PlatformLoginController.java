package com.homi.platform.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.homi.common.lib.annotation.LoginLog;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.redis.RedisKey;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.menu.vo.AsyncRoutesVO;
import com.homi.platform.service.service.perms.PlatformUserService;
import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.dto.login.PlatformLoginSmsSendDTO;
import com.homi.platform.web.dto.login.PlatformLoginUpdateDTO;
import com.homi.platform.web.dto.login.TokenRefreshDTO;
import com.homi.platform.web.dto.login.PlatformLoginDTO;
import com.homi.platform.web.service.PlatformAuthService;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private final PlatformUserService platformUserService;
    private final StringRedisTemplate redisTemplate;

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

    @GetMapping("/captcha/{phone}")
    public void captcha(@PathVariable("phone") String phone, HttpServletResponse response) throws IOException {
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 80, randomGenerator, 100);
        String code = lineCaptcha.getCode();
        redisTemplate.opsForValue().set(RedisKey.CAPTCHA.format(phone), code, RedisKey.CAPTCHA.getTimeout(), RedisKey.CAPTCHA.getUnit());
        response.setContentType("image/png");
        ImageIO.write(lineCaptcha.getImage(), "png", response.getOutputStream());
    }

    @PostMapping("/login/sms/send")
    public ResponseResult<Boolean> sendSmsCode(@Valid @RequestBody PlatformLoginSmsSendDTO dto) {
        String captchaCode = redisTemplate.opsForValue().get(RedisKey.CAPTCHA.format(dto.getPhone()));
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(dto.getCaptcha())) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }

        if (platformUserService.getUserByPhone(dto.getPhone()) == null) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }

        String verifyCode = platformAuthService.getSmsVerifyCode(dto.getPhone());
        redisTemplate.opsForValue().set(RedisKey.SMS_CODE.format(dto.getPhone()), verifyCode, RedisKey.SMS_CODE.getTimeout(), RedisKey.SMS_CODE.getUnit());

        log.info("平台发送短信验证码，手机号：{}，验证码：{}", dto.getPhone(), verifyCode);
        return ResponseResult.ok(Boolean.TRUE);
    }

    @PostMapping("/login/update")
    public ResponseResult<Boolean> updatePassword(@Valid @RequestBody PlatformLoginUpdateDTO dto) {
        String verifyCode = redisTemplate.opsForValue().get(RedisKey.SMS_CODE.format(dto.getPhone()));
        if (verifyCode == null) {
            throw new BizException("请先发送验证码");
        }
        if (!dto.getVerifyCode().equals(verifyCode)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }
        if (platformUserService.getUserByPhone(dto.getPhone()) == null) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        return ResponseResult.ok(platformUserService.updateUserPasswordByPhone(dto.getPhone(), dto.getPassword()));
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
