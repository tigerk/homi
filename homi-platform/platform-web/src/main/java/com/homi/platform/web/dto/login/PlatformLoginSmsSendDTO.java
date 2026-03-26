package com.homi.platform.web.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlatformLoginSmsSendDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "图形验证码不能为空")
    private String captcha;
}
