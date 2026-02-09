package com.homi.common.lib.enums.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContractNatureEnum {
    /*
     * 合同性质：1=新签，2=续签，3=转租，4=换房
     */
    NEW_SIGN(1, "新签"),
    RENEWAL(2, "续签"),
    SUBLET(3, "转租"),
    RELOCATION(4, "换房");

    private final Integer code;
    private final String name;
}
