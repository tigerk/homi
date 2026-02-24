package com.homi.common.lib.enums.lease;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaseRentDueTypeEnum {
    /**
     * 收租类型：1=提前，2=固定，3=延后
     */
    EARLY(1, "提前"),
    FIXED(2, "固定"),
    LATE(3, "延后"),
    ;

    private final Integer code;
    private final String name;
}
