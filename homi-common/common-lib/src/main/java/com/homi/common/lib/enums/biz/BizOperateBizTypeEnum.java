package com.homi.common.lib.enums.biz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业务日志业务类型枚举")
public enum BizOperateBizTypeEnum {
    LEASE("LEASE", "租客合同"),
    LEASE_CONTRACT_DOC("LEASE_CONTRACT_DOC", "租客签约合同"),
    LEASE_CHECKOUT("LEASE_CHECKOUT", "租客退租单"),
    OWNER_CONTRACT("OWNER_CONTRACT", "业主合同"),
    OWNER_CONTRACT_DOC("OWNER_CONTRACT_DOC", "业主签约合同"),
    OWNER_CONTRACT_CHECKOUT("OWNER_CONTRACT_CHECKOUT", "业主合同退房单"),
    OWNER_PAYABLE_BILL("OWNER_PAYABLE_BILL", "包租业主应付单"),
    OWNER_SETTLEMENT_BILL("OWNER_SETTLEMENT_BILL", "轻托管业主结算单"),
    HOUSE("HOUSE", "房源"),
    ROOM("ROOM", "房间");

    private final String code;
    private final String name;
}
