package com.homi.model.company.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户返回VO")
public class UserLiteVO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名（登录名）")
    private String username;

    private Long companyId;

    private Long deptId;

    /**
     * 邮箱号
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;
}
