package com.homi.common.lib.enums.pay;

import java.util.Arrays;

public enum PayChannelEnum {
    YEEPAY("yeepay"),
    ALIPAY("alipay"),
    WECHAT("wechat");

    private final String code;

    PayChannelEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean supports(String code) {
        return Arrays.stream(values()).anyMatch(item -> item.code.equalsIgnoreCase(code));
    }
}
