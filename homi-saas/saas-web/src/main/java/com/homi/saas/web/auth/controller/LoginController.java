package com.homi.saas.web.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.annotation.LoginLog;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.event.OperationLogEvent;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.redis.RedisKey;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.RequestResultEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.utils.ServletUtils;
import com.homi.common.lib.utils.SpringUtils;
import com.homi.external.mail.MailClient;
import com.homi.external.sms.SmsClient;
import com.homi.model.company.dto.CompanySwitchDTO;
import com.homi.model.company.dto.UserCompanyListDTO;
import com.homi.model.menu.vo.AsyncRoutesVO;
import com.homi.model.dao.entity.User;
import com.homi.saas.web.auth.dto.account.UserProfileUpdateDTO;
import com.homi.saas.web.auth.dto.login.*;
import com.homi.saas.web.auth.service.AuthService;
import com.homi.saas.web.auth.service.WechatAuthService;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.company.CompanyUserService;
import com.homi.service.service.sys.UserService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
    private final WechatAuthService wechatAuthService;
    private final CompanyUserService companyUserService;
    private final UserService userService;

    private final StringRedisTemplate redisTemplate;

    private final MailClient mailClient;

    // 短信客户端
    private final SmsClient smsClient;

    /**
     * 用户登录
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/29 11:44
     *
     * @param loginDTO 参数说明
     * @return com.homi.common.model.response.ResponseResult<com.homi.saas.web.admin.auth.vo.login.UserLoginVO>
     */
    @LoginLog
    @PostMapping("/saas/login")
    @Log(title = "登录", operationType = OperationTypeEnum.OTHER)
    public ResponseResult<UserLoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserLoginVO userLogin = authService.checkUserLogin(loginDTO);

        return ResponseResult.ok(authService.login(userLogin));
    }

    @PostMapping("/saas/wechat/login")
    @Log(title = "微信登录", operationType = OperationTypeEnum.OTHER)
    public ResponseResult<UserLoginVO> wechatLogin(@Valid @RequestBody WechatLoginDTO wechatLoginDTO) {
        return ResponseResult.ok(wechatAuthService.loginByCode(wechatLoginDTO.getCode()));
    }

    @PostMapping("/saas/wechat/bind")
    public ResponseResult<UserLoginVO> wechatBind(@Valid @RequestBody WechatBindDTO wechatBindDTO) {
        return ResponseResult.ok(wechatAuthService.bindAndLogin(wechatBindDTO.getCode(), wechatBindDTO.getUsername(), wechatBindDTO.getPassword()));
    }

    @GetMapping("/saas/captcha/{username}")
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

    @PostMapping("/saas/token/refresh")
    @LoginLog
    public ResponseResult<UserLoginVO> refresh(@RequestBody TokenRefreshDTO req) {
        Object loginIdByToken = StpUtil.getLoginIdByToken(req.getRefreshToken());
        if (Objects.isNull(loginIdByToken)) {
            throw new BizException(ResponseCodeEnum.TOKEN_ERROR);
        }

        UserLoginVO userLoginVO = LoginManager.getCurrentUserByUserId(Long.valueOf(loginIdByToken.toString()));

        return ResponseResult.ok(userLoginVO);
    }

    @PostMapping("/saas/logout")
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
     * @return com.homi.common.model.response.ResponseResult<com.homi.saas.web.admin.auth.vo.login.UserLoginVO>
     */
    @LoginLog
    @PostMapping("/saas/login/current")
    public ResponseResult<UserLoginVO> getCurrentUser() {
        return ResponseResult.ok(LoginManager.getCurrentUser());
    }

    @PostMapping("/saas/get-async-routes")
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
     * @param companySwitchDTO 参数说明
     * @return com.homi.common.model.response.ResponseResult<com.homi.saas.web.admin.auth.vo.login.UserLoginVO>
     */
    @PostMapping("/saas/switchCompany")
    public ResponseResult<UserLoginVO> switchCompany(@Valid @RequestBody CompanySwitchDTO companySwitchDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        // 校验用户是否有这个公司权限
        if (!companyUserService.userHasCompany(currentUser.getId(), companySwitchDTO.getCompanyId())) {
            throw new BizException("无效的公司ID");
        }

        return ResponseResult.ok(authService.loginWithCompanyId(currentUser.getId(), companySwitchDTO.getCompanyId()));
    }

    @PostMapping("/saas/login/sms")
    @Log(title = "短信登录", operationType = OperationTypeEnum.OTHER)
    public ResponseResult<UserLoginVO> smsLogin(@Valid @RequestBody SmsLoginDTO smsLoginDTO) {
        String verifyCode = redisTemplate.opsForValue().get(RedisKey.SMS_CODE.format(Long.valueOf(smsLoginDTO.getPhone())));
        if (verifyCode == null) {
            throw new BizException("请先发送验证码");
        }

        if (!smsLoginDTO.getVerifyCode().equals(verifyCode)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }

        var user = userService.getUserByPhone(smsLoginDTO.getPhone());
        if (user == null) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }

        return ResponseResult.ok(authService.loginByUserId(user.getId()));
    }

    @PostMapping("/saas/login/sms/send")
    @Log(title = "发送登录短信", operationType = OperationTypeEnum.OTHER)
    public ResponseResult<Boolean> sendSmsCode(@Valid @RequestBody LoginSmsSendDTO dto) {
        String phone = dto.getPhone();
        String captchaCode = redisTemplate.opsForValue().get(RedisKey.CAPTCHA.format(phone));
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(dto.getCaptcha())) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }

        String verifyCode = getSmsVerifyCode(phone);
        // 保存到 Redis，key: sms:phone，value: code，有效期10分钟
        redisTemplate.opsForValue().set(RedisKey.SMS_CODE.format(phone), verifyCode, RedisKey.SMS_CODE.getTimeout(), RedisKey.SMS_CODE.getUnit());

        log.info("发送短信验证码，手机号：{}，验证码：{}", phone, verifyCode);

        // 发送短信验证码
//        smsClient.send(phone.toString(), "", "SMS_256500000", verifyCode);

        return ResponseResult.ok(Boolean.TRUE);
    }

    @PostMapping("/saas/login/update")
    public ResponseResult<Boolean> updatePassword(@RequestBody LoginUpdateDTO loginUpdate) {
        String verifyCode = redisTemplate.opsForValue().get(RedisKey.SMS_CODE.format(loginUpdate.getPhone()));
        if (verifyCode == null) {
            throw new BizException("请先发送验证码");
        }

        if (!loginUpdate.getVerifyCode().equals(verifyCode)) {
            throw new BizException("验证码错误");
        }

        User user = userService.getUserByPhone(loginUpdate.getPhone());
        if (user == null) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }

        boolean success = false;
        String errorMsg = null;
        try {
            Boolean updated = authService.updateUserPassword(loginUpdate.getPhone(), loginUpdate.getPassword());
            success = Boolean.TRUE.equals(updated);
            return ResponseResult.ok(updated);
        } catch (Exception ex) {
            errorMsg = ex.getMessage();
            throw ex;
        } finally {
            publishPasswordResetOperationLog(user, loginUpdate.getPhone(), success, errorMsg);
        }
    }

    @PostMapping("/saas/register")
    @Log(title = "注册账号", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Boolean> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        String verifyCode = redisTemplate.opsForValue().get(RedisKey.SMS_CODE.format(registerDTO.getPhone()));
        if (verifyCode == null) {
            throw new BizException("请先发送验证码");
        }

        if (!registerDTO.getVerificationCode().equals(verifyCode)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }

        return ResponseResult.ok(authService.register(registerDTO));
    }

    private void publishPasswordResetOperationLog(User user, String phone, boolean success, String errorMsg) {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        OperationLogEvent operationLog = new OperationLogEvent();
        operationLog.setTitle("密码重置");
        operationLog.setOperationType(OperationTypeEnum.UPDATE.getCode());
        operationLog.setOperatorType(0);
        operationLog.setMethod(LoginController.class.getName() + ".updatePassword()");
        operationLog.setRequestMethod(ServletUtils.getRequest().getMethod());
        operationLog.setRequestUrl(CharSequenceUtil.sub(ServletUtils.getRequest().getRequestURI(), 0, 255));
        operationLog.setIpAddress(ServletUtils.getClientIP());
        operationLog.setOs(userAgent.getOperatingSystem().getName());
        operationLog.setBrowser(userAgent.getBrowser().getName());
        operationLog.setUserId(user.getId());
        operationLog.setUsername(user.getUsername());
        operationLog.setCompanyId(getFirstCompanyId(user.getId()));
        operationLog.setParam(CharSequenceUtil.sub("{\"phone\":\"" + phone + "\"}", 0, 2000));
        operationLog.setStatus(success ? RequestResultEnum.SUCCESS.getCode() : RequestResultEnum.FAILURE.getCode());
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            operationLog.setErrorMsg(CharSequenceUtil.sub(errorMsg, 0, 2000));
        }
        operationLog.setRequestAt(DateUtil.date());
        operationLog.setCostTime(0L);

        SpringUtils.context().publishEvent(operationLog);
    }

    private Long getFirstCompanyId(Long userId) {
        List<UserCompanyListDTO> companyList = companyUserService.getCompanyListByUserId(userId);
        if (companyList.isEmpty()) {
            return null;
        }
        return companyList.get(0).getCompanyId();
    }

    /**
     * 获取当前账户的个人信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/9 13:48
     *
     * @return com.homi.common.model.response.ResponseResult<com.homi.saas.web.admin.auth.vo.login.UserLoginVO>
     */
    @PostMapping("/saas/login/profile/get")
    public ResponseResult<UserProfileUpdateDTO> getLoginUserProfile() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();

        return ResponseResult.ok(authService.getUserProfile(currentUser.getId()));
    }

    /**
     * 更新当前账户的个人信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/23 11:44
     *
     * @param userProfileUpdateDTO 参数说明
     * @return com.homi.common.lib.response.ResponseResult<com.homi.saas.web.auth.vo.login.UserLoginVO>
     */
    @PostMapping("/saas/login/profile/update")
    @Log(title = "更新个人信息", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Boolean> updateLoginUserProfile(@RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
        return ResponseResult.ok(authService.updateUserProfile(userProfileUpdateDTO));
    }

    @PostMapping("/saas/account/password/update")
    @Log(title = "修改密码", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Boolean> updatePassword(@Valid @RequestBody com.homi.saas.web.auth.dto.account.AccountPasswordUpdateDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(authService.updateUserPasswordByUserId(currentUser.getId(), dto.getOldPassword(), dto.getNewPassword()));
    }

    @PostMapping("/saas/account/phone/old/sms/send")
    @Log(title = "发送原手机号验证码", operationType = OperationTypeEnum.OTHER)
    public ResponseResult<Boolean> sendOldPhoneChangeSms() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        String phone = currentUser.getPhone();
        if (CharSequenceUtil.isBlank(phone)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "当前账号未绑定手机号");
        }
        String verifyCode = getSmsVerifyCode(phone);
        redisTemplate.opsForValue().set(RedisKey.ACCOUNT_PHONE_OLD_CODE.format(phone), verifyCode, RedisKey.ACCOUNT_PHONE_OLD_CODE.getTimeout(), RedisKey.ACCOUNT_PHONE_OLD_CODE.getUnit());

        log.info("发送原手机号验证码，手机号：{}，验证码：{}", phone, verifyCode);
        // smsClient.send(phone, "", "SMS_256500000", verifyCode);

        return ResponseResult.ok(Boolean.TRUE);
    }

    /**
     * 生成短信验证码，用于手机号变更、密码更新等场景
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/25 09:10
     *
     * @param phone 参数说明
     * @return java.lang.String
     */
    private String getSmsVerifyCode(String phone) {
        String rateKey = RedisKey.SMS_RATE_LIMIT.format(phone);
        Boolean rateLimit = redisTemplate.hasKey(rateKey);
        if (Boolean.TRUE.equals(rateLimit)) {
            throw new BizException("发送过于频繁，请稍后再试");
        }
        redisTemplate.opsForValue().set(rateKey, "1", RedisKey.SMS_RATE_LIMIT.getTimeout(), RedisKey.SMS_RATE_LIMIT.getUnit());

        RandomGenerator verifyCodeGenerator = new RandomGenerator("0123456789", 4);

        return verifyCodeGenerator.generate();
    }

    @PostMapping("/saas/account/phone/new/sms/send")
    @Log(title = "发送新手机号验证码", operationType = OperationTypeEnum.OTHER)
    public ResponseResult<Boolean> sendNewPhoneChangeSms(@RequestBody com.homi.saas.web.auth.dto.account.AccountPhoneUpdateDTO dto) {
        String phone = dto.getPhone();
        if (CharSequenceUtil.isBlank(phone)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "手机号不能为空");
        }

        String verifyCode = getSmsVerifyCode(phone);
        redisTemplate.opsForValue().set(RedisKey.ACCOUNT_PHONE_NEW_CODE.format(phone), verifyCode, RedisKey.ACCOUNT_PHONE_NEW_CODE.getTimeout(), RedisKey.ACCOUNT_PHONE_NEW_CODE.getUnit());

        log.info("发送新手机号验证码，手机号：{}，验证码：{}", phone, verifyCode);
        // smsClient.send(phone, "", "SMS_256500000", verifyCode);

        return ResponseResult.ok(Boolean.TRUE);
    }

    @PostMapping("/saas/account/email/code/send")
    @Log(title = "发送更换邮箱验证码", operationType = OperationTypeEnum.OTHER)
    public ResponseResult<Boolean> sendEmailChangeCode(@RequestBody com.homi.saas.web.auth.dto.account.AccountEmailUpdateDTO dto) {
        String email = dto.getEmail();
        if (CharSequenceUtil.isBlank(email)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "邮箱不能为空");
        }
        String rateKey = RedisKey.SMS_RATE_LIMIT.format(email);
        Boolean rateLimit = redisTemplate.hasKey(rateKey);
        if (rateLimit) {
            throw new BizException("发送过于频繁，请稍后再试");
        }
        redisTemplate.opsForValue().set(rateKey, "1", RedisKey.SMS_RATE_LIMIT.getTimeout(), RedisKey.SMS_RATE_LIMIT.getUnit());

        RandomGenerator verifyCodeGenerator = new RandomGenerator("0123456789", 4);
        String verifyCode = verifyCodeGenerator.generate();
        redisTemplate.opsForValue().set(RedisKey.ACCOUNT_EMAIL_CODE.format(email), verifyCode, RedisKey.ACCOUNT_EMAIL_CODE.getTimeout(), RedisKey.ACCOUNT_EMAIL_CODE.getUnit());

        log.info("发送更换邮箱验证码，邮箱：{}，验证码：{}", email, verifyCode);
        mailClient.send(email, "邮箱验证码", "您的邮箱验证码为：" + verifyCode + "，10分钟内有效。");

        return ResponseResult.ok(Boolean.TRUE);
    }

    @PostMapping("/saas/account/phone/update")
    @Log(title = "更换手机号", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Boolean> updatePhone(@Valid @RequestBody com.homi.saas.web.auth.dto.account.AccountPhoneChangeDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        String oldPhone = currentUser.getPhone();
        if (CharSequenceUtil.isBlank(oldPhone)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "当前账号未绑定手机号");
        }
        String oldVerifyCode = redisTemplate.opsForValue().get(RedisKey.ACCOUNT_PHONE_OLD_CODE.format(oldPhone));
        if (oldVerifyCode == null) {
            throw new BizException("请先发送原手机号验证码");
        }
        if (!dto.getOldVerifyCode().equals(oldVerifyCode)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }
        String newVerifyCode = redisTemplate.opsForValue().get(RedisKey.ACCOUNT_PHONE_NEW_CODE.format(dto.getNewPhone()));
        if (newVerifyCode == null) {
            throw new BizException("请先发送新手机号验证码");
        }
        if (!dto.getNewVerifyCode().equals(newVerifyCode)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }
        boolean updated = authService.updateUserPhone(currentUser.getId(), dto.getNewPhone());
        if (updated) {
            currentUser.setUsername(dto.getNewPhone());
            currentUser.setPhone(dto.getNewPhone());
            LoginManager.updateLoginUserInfo(currentUser);
        }
        return ResponseResult.ok(updated);
    }

    @PostMapping("/saas/account/email/update")
    @Log(title = "更换邮箱", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Boolean> updateEmail(@Valid @RequestBody com.homi.saas.web.auth.dto.account.AccountEmailUpdateDTO dto) {
        String verifyCode = redisTemplate.opsForValue().get(RedisKey.ACCOUNT_EMAIL_CODE.format(dto.getEmail()));
        if (verifyCode == null) {
            throw new BizException("请先发送验证码");
        }
        if (!dto.getVerifyCode().equals(verifyCode)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        boolean updated = authService.updateUserEmail(currentUser.getId(), dto.getEmail());
        if (updated) {
            currentUser.setEmail(dto.getEmail());
            LoginManager.updateLoginUserInfo(currentUser);
        }
        return ResponseResult.ok(updated);
    }
}
