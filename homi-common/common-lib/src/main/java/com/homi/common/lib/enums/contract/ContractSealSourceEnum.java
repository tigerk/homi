package com.homi.common.lib.enums.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContractSealSourceEnum {
    /*
     * 来源:1=自有图片,2=法大大,3=E签宝,4=其他第三方
     */
    SELF(1, "自有图片"),
    FADADA(2, "法大大"),
    EQIBAO(3, "E签宝"),
    OTHER(4, "其他第三方"),
    ;

    private final Integer code;
    private final String name;
}
