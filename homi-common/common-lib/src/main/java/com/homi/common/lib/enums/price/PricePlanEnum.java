package com.homi.common.lib.enums.price;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/30
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PricePlanEnum {
    /**
     * { label: "月付", value: 0 },
     * { label: "2月付", value: 1 },
     * { label: "季付", value: 2 },
     * { label: "半年付", value: 3 },
     * { label: "年付", value: 4 }
     */
    MONTH(0, "月付"),
    TWO_MONTH(1, "2月付"),
    QUARTER(2, "季付"),
    HALF_YEAR(3, "半年付"),
    YEAR(4, "年付"),
    ;

    private final Integer code;

    private final String name;


}
