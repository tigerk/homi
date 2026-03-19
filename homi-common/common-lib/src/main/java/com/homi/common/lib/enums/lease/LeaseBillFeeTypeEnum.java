package com.homi.common.lib.enums.lease;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaseBillFeeTypeEnum {
    RENTAL("RENTAL", "租金"),
    DEPOSIT("DEPOSIT", "押金"),
    OTHER_FEE("OTHER_FEE", "其他费用");

    private final String code;
    private final String label;

    public static String getLabelByCode(String code) {
        for (LeaseBillFeeTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item.label;
            }
        }
        return OTHER_FEE.label;
    }
}
