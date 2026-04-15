package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主结算单状态枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主结算单状态枚举")
public enum OwnerSettlementBillStatusEnum {
    NORMAL(1, "正常");

    private final Integer code;
    private final String name;
}
