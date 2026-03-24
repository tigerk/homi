package com.homi.common.lib.enums.lease;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaseBillStatusEnum {
    NORMAL(1, "正常"),
    VOIDED(2, "已作废");

    private final Integer code;
    private final String name;
}
