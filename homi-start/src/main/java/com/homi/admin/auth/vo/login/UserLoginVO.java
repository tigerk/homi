package com.homi.admin.auth.vo.login;

import com.homi.domain.vo.menu.AsyncRoutesVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO {

    /**
     * token值
     */
    private String accessToken;

    private String refreshToken;

    @Schema(description = "token过期时间")
    private Long expires;

    /**
     * id
     */
    private Long id;


    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型，参考UserTypeEnum
     */
    private Integer userType;

    /**
     * companyId 为空 → 平台用户
     */
    private Long companyId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 简介
     */
    private String intro;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDateTime birthday;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;

    /**
     * IP来源
     */
    @Schema(description = "IP来源")
    private String ipSource;

    /**
     * 角色集合
     */
    @Schema(description = "角色集合")
    private List<String> roles;

    /**
     * 按钮权限
     */
    @Schema(description = "按钮权限")
    private List<String> permissions;

    /**
     * 菜单列表
     */
    @Schema(description = "菜单列表")
    private List<AsyncRoutesVO> asyncRoutes;
}
