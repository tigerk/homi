package com.homi.saas.web.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.common.lib.exception.BizException;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 拦截后台请求
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 12:16
 */
@Slf4j
public class LoginManager {
    private LoginManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取当前登录用户 ID（Long 类型）
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户 ID（String 类型）
     */
    public static String getUserIdStr() {
        return StpUtil.getLoginIdAsString();
    }

    /**
     * 获取当前登录的用户对象（登录时需手动 set 进去）
     */
    public static UserLoginVO getCurrentUser() {
        UserLoginVO userLoginVO = (UserLoginVO) StpUtil.getSession().get(SaSession.USER);
        if (userLoginVO == null) {
            throw new BizException("未找到当前登录用户信息");
        }

        return userLoginVO;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getCurrentRoles() {
        return (List<String>) StpUtil.getSession().get(SaSession.ROLE_LIST);
    }

    public static boolean hasRole(String role) {
        List<String> roles = getCurrentRoles();
        return roles != null && roles.contains(role);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getCurrentPermissions() {
        return (List<String>) StpUtil.getSession().get(SaSession.PERMISSION_LIST);
    }

    /**
     * 获取当前登录用户 Token 信息
     */
    public static SaTokenInfo getTokenInfo() {
        return StpUtil.getTokenInfo();
    }
}
