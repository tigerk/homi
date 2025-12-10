package com.homi.common.lib.enums.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContractTemplateStatusEnum {
    /*
     * 合同模板状态：0=未启用，1=已启用
     */
    UNEFFECTIVE(0, "未启用"),
    EFFECTIVE(1, "已启用");

    private final Integer code;
    private final String name;
}
