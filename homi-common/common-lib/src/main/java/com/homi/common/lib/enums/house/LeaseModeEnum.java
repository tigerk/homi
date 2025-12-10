package com.homi.common.lib.enums.house;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaseModeEnum {
    /**
     * 运营模式：集中式 or 分散式
     */
    UNKNOWN(0, "未知"),
    FOCUS(1, "集中式"),
    SCATTER(2, "分散式");

    private final Integer code;

    private final String name;
}
