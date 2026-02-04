package com.homi.saas.web.auth.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录
 *
 * @author tk
 * @since 2026-02-04
 */
@Data
public class WechatLoginDTO {
    @NotBlank(message = "code不能为空")
    private String code;
}
