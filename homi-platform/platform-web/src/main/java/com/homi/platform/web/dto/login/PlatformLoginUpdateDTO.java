package com.homi.platform.web.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlatformLoginUpdateDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;
}
