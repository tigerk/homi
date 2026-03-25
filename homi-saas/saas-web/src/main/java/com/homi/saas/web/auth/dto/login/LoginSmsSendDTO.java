package com.homi.saas.web.auth.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginSmsSendDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "图形验证码不能为空")
    private String captcha;
}
