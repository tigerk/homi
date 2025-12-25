package com.homi.saas.web.auth.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TokenRefreshDTO {
    @Schema(description = "刷新令牌")
    private String refreshToken;
}
