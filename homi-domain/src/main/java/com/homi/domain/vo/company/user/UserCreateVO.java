package com.homi.domain.vo.company.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "用户创建VO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateVO {
    @Schema(description = "公司用户ID")
    private Long companyUserId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    private Boolean existed = Boolean.FALSE;
}
