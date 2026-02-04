package com.homi.saas.web.auth.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 短信验证码登录
 *
 * @author tk
 * @since 2026-02-04
 */
@Data
public class SmsLoginDTO {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}
