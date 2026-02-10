package com.homi.saas.web.auth.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountPhoneChangeDTO {
    @NotBlank(message = "原手机号验证码不能为空")
    private String oldVerifyCode;

    @NotBlank(message = "新手机号不能为空")
    private String newPhone;

    @NotBlank(message = "新手机号验证码不能为空")
    private String newVerifyCode;
}
