package com.homi.common.lib.enums.tenant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TenantFirstBillDayEnum {
    /**
     * 首期账单收租日：0=跟随合同起租日，1=跟随合同创建日
     */
    FOLLOW_CONTRACT_START(0, "跟随合同起租日"),
    FOLLOW_CONTRACT_CREATE(1, "跟随合同创建日");

    private final Integer code;
    private final String name;
}
