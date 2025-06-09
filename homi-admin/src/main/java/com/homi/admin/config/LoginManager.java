package com.homi.admin.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.exception.BizException;
import com.homi.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 拦截后台请求
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 12:16
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class LoginManager {

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
    public static SysUser getCurrentUser() {
        Object user = StpUtil.getSession().get(SaSession.USER);
        if (user == null) {
            throw new BizException("未找到当前登录用户信息");
        }
        return (SysUser) user;
    }

    /**
     * 获取当前登录用户 Token 信息
     */
    public static SaTokenInfo getTokenInfo() {
        return StpUtil.getTokenInfo();
    }
}
