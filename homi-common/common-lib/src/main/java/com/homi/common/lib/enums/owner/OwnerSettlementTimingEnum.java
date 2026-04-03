package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主分账时间枚举")
public enum OwnerSettlementTimingEnum {
    TENANT_PAYMENT_REALTIME("TENANT_PAYMENT_REALTIME", "租客支付实时分账"),
    LEASE_START_GENERATE_BILL("LEASE_START_GENERATE_BILL", "起租日生成账单");

    private final String code;
    private final String name;
}
