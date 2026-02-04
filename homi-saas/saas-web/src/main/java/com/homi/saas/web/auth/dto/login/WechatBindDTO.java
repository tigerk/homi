package com.homi.saas.web.auth.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序绑定账号
 *
 * @author tk
 * @since 2026-02-04
 */
@Data
public class WechatBindDTO {
    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
