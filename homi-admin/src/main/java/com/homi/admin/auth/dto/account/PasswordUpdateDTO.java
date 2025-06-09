package com.homi.admin.auth.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateDTO {

    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String newPassword;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确！")
    private String verificationCode;
}
