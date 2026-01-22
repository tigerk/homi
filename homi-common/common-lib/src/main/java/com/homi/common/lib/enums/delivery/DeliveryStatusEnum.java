package com.homi.common.lib.enums.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Getter
@AllArgsConstructor
public enum DeliveryStatusEnum {
    /**
     * 作废
     */
    CANCELLED(0, "作废"),

    /**
     * 草稿
     */
    DRAFT(1, "草稿"),

    /**
     * 已签署
     */
    SIGNED(2, "已签署");

    private final int code;
    private final String name;
}
