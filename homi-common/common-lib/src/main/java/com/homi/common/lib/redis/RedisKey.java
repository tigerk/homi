package com.homi.common.lib.redis;

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

    // 房源编号计数器
    HOUSE_CODE_COUNTER("house:count:%s:%s", 86400, TimeUnit.SECONDS),

    // 登录用户公司
    LOGIN_USER_COMPANY("login:user:company:%s", 30, TimeUnit.DAYS);


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
