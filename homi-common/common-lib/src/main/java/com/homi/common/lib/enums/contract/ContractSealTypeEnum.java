package com.homi.common.lib.enums.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContractSealTypeEnum {
    /*
     * 印章类型:1=企业,2=个人
     */
    COMPANY(1, "企业"),
    PERSONAL(2, "个人"),
    ;

    private final Integer code;
    private final String name;
}
