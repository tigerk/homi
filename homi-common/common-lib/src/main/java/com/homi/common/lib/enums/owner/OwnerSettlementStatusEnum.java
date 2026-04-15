package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主结算状态枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主结算状态枚举")
public enum OwnerSettlementStatusEnum {
    UNSETTLED(0, "未结算"),
    PART_SETTLED(1, "部分结算"),
    SETTLED(2, "已结算");

    private final Integer code;
    private final String name;
}
