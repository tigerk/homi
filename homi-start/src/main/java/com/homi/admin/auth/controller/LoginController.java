package com.homi.admin.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.homi.admin.auth.dto.login.LoginDTO;
import com.homi.admin.auth.dto.login.LoginUpdateDTO;
import com.homi.admin.auth.dto.login.TokenRefreshDTO;
import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.LoginLog;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.enums.RedisKey;
import com.homi.domain.vo.menu.AsyncRoutesVO;
import com.homi.exception.BizException;
import com.homi.external.aliyun.SmsClient;
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
    private final SmsClient smsClient;

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

    @PostMapping("/admin/login/sms/send")
    public ResponseResult<Boolean> sendSmsCode(@RequestParam("phone") Long phone) {
        RandomGenerator verifyCodeGenerator = new RandomGenerator("0123456789", 4);
        String verifyCode = verifyCodeGenerator.generate();
        // 保存到 Redis，key: sms:phone，value: code，有效期10分钟
        redisTemplate.opsForValue().set(RedisKey.SMS_CODE.format(phone), verifyCode, RedisKey.SMS_CODE.getTimeout(), RedisKey.SMS_CODE.getUnit());

        log.info("发送短信验证码，手机号：{}，验证码：{}", phone, verifyCode);

        // 发送短信验证码
//        smsClient.send(phone.toString(), "", "SMS_256500000", verifyCode);

        return ResponseResult.ok(Boolean.TRUE);
    }

    @PostMapping("/admin/login/update")
    public ResponseResult<Boolean> sendSmsCode(@RequestBody LoginUpdateDTO loginUpdate) {
        String verifyCode = redisTemplate.opsForValue().get(RedisKey.SMS_CODE.format(loginUpdate.getPhone()));
        if (verifyCode == null) {
            throw new BizException("请先发送验证码");
        }

        if (!loginUpdate.getVerifyCode().equals(verifyCode)) {
            throw new BizException("验证码错误");
        }

        // 更新用户密码
        return ResponseResult.ok(authService.updateUserPassword(loginUpdate.getPhone(), loginUpdate.getPassword()));
    }
}
