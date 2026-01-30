package com.homi.saas.web.auth.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;

    @Schema(description = "公司ID", hidden = true)
    private Long companyId;
}
