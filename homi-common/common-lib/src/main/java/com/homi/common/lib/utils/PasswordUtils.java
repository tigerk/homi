package com.homi.common.lib.utils;

import cn.dev33.satoken.secure.SaSecureUtil;

/**
 * 字符串工具类
 *
 * @author ruoyi
 */
public final class PasswordUtils {
    /**
     * 密码加密，全部采用 md5方式来加密用户密码
     */
    public static String encryptPassword(String password) {
        return SaSecureUtil.md5(password);
    }
}
