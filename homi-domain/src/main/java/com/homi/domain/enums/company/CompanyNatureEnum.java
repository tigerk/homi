package com.homi.domain.enums.company;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompanyNatureEnum {
    /**
     * 企业或者个人
     */
    ENTERPRISE(1, "企业"),
    PERSONAL(2, "个人");

    private final int code;
    private final String message;
}
