package com.homi.common.lib.enums.price;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/30
 */

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum PriceMethodEnum {
    /**
     * 计算方式枚举
     */
    FIXED(1, "按固定金额"),
    RATIO(2, "按租金比例"),
    ;

    private final Integer code;

    private final String name;

    public static PriceMethodEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        return EnumUtil.getBy(PriceMethodEnum::getCode, code);
    }
}
