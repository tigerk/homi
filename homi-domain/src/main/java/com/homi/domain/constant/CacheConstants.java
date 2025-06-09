package com.homi.domain.constant;

/**
 * 描述
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:22
 */
public class CacheConstants {

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";
}
