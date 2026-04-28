package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主账单业务场景枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主账单业务场景枚举")
public enum OwnerBillSceneEnum {
    REGULAR("REGULAR", "正常账单"),
    REALTIME_SETTLEMENT("REALTIME_SETTLEMENT", "租客支付实时分账"),
    CHECKOUT_PENALTY("CHECKOUT_PENALTY", "业主退房违约金");

    private final String code;
    private final String name;
}
