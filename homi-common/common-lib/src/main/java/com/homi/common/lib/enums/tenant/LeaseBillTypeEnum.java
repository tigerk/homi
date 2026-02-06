package com.homi.common.lib.enums.tenant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaseBillTypeEnum {
    RENT(1, "租金"),
    DEPOSIT(2, "押金"),
    OTHER_FEE(3, "杂费"),
    RELEASE(4, "退租结算"),
    DEPOSIT_CARRY_IN(5, "押金结转入"),
    DEPOSIT_CARRY_OUT(6, "押金结转出");

    private final Integer code;
    private final String name;
}
