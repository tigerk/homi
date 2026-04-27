package com.homi.common.lib.enums.biz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业务日志业务类型枚举")
public enum BizOperateBizTypeEnum {
    LEASE("LEASE", "租客合同"),
    LEASE_CHECKOUT("LEASE_CHECKOUT", "租客退租单"),
    OWNER_PAYABLE_BILL("OWNER_PAYABLE_BILL", "包租业主应付单"),
    OWNER_SETTLEMENT_BILL("OWNER_SETTLEMENT_BILL", "轻托管业主结算单");

    private final String code;
    private final String name;
}
