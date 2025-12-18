package com.homi.common.lib.enums.tenant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TenantBillTypeEnum {
    RENT(1, "租金"),
    DEPOSIT(2, "押金"),
    OTHER_FEE(3, "杂费"),
    RELEASE(4, "退租结算");

    private final Integer code;
    private final String name;
}
