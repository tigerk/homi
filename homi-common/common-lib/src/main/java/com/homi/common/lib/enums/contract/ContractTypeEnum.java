package com.homi.common.lib.enums.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContractTypeEnum {
    /*
     * 合同模板类型：1=租客、2=业主、3=预定
     */
    TENANT(1, "租客"),
    OWNER(2, "房东"),
    BOOKING(3, "预定");

    private final Integer code;
    private final String name;
}
