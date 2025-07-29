package com.homi.domain.enums.common;

import lombok.Getter;

/**
 * 日志
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:28
 */
@Getter
public enum RequestResultEnum {

    /**
     * 成功
     */
    SUCCESS(0, "成功"),

    /**
     * 失败
     */
    FAILURE(-1, "失败");

    private final int code;
    private final String message;

    RequestResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "OperationResult{" + "code=" + code + ", message='" + message + '\'' + '}';
    }
}
