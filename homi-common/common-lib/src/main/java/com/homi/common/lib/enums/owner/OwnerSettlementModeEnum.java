package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主轻托管结算模式枚举")
public enum OwnerSettlementModeEnum {
    FIXED("FIXED", "固定保底"),
    SHARE_GROSS("SHARE_GROSS", "毛收分成"),
    SHARE_NET("SHARE_NET", "净收分成"),
    GUARANTEE_PLUS_SHARE("GUARANTEE_PLUS_SHARE", "保底加分成"),
    AGENCY("AGENCY", "代收代付");

    private final String code;
    private final String name;
}
