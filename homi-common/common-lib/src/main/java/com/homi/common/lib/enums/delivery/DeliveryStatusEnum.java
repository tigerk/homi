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
    CANCELLED(-1, "作废"),

    /**
     * 待填写
     */
    DRAFT(0, "待填写"),

    /**
     * 已填写
     */
    COMPLETED(1, "已填写"),

    /**
     * 已签署
     */
    SIGNED(2, "已签署");

    private final int code;
    private final String name;
}
