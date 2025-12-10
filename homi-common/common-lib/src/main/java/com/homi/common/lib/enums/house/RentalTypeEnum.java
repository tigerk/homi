package com.homi.common.lib.enums.house;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RentalTypeEnum {
    ENTIRE(1, "整租"),
    SHARED(2, "合租");

    private final Integer code;

    private final String name;
}
