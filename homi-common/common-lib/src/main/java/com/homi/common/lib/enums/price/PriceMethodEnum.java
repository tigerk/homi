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
public enum PriceMethodEnum {
    /**
     * 计算方式枚举
     */
    FIXED(0, "按固定金额"),
    RENT(1, "随房租付"),
    ;

    private final Integer code;

    private final String name;


}
