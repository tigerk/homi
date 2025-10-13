package com.homi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum RedisKey {
    // 示例：短信验证码
    SMS_CODE("sms:code:%s", 10, TimeUnit.MINUTES),

    // 示例：图形验证码
    CAPTCHA("captcha:%s", 5, TimeUnit.MINUTES),

    LOGIN_REFRESH_TOKEN("login:refresh:token:%s", 7, TimeUnit.DAYS),

    // 示例：登录token
    LOGIN_TOKEN("login:token:%s", 30, TimeUnit.DAYS);


    private final String keyPattern;
    private final long timeout;
    private final TimeUnit unit;

    /**
     * 格式化 Key，比如填入手机号、UUID 等
     */
    public String format(Object... args) {
        return "homi:" + String.format(this.keyPattern, args);
    }
}
