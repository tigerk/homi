package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "包租业主应付单付款记录状态枚举")
public enum OwnerPayableBillPaymentRecordStatusEnum {
    PENDING_APPROVAL(0, "待审核"),
    SUCCESS(1, "付款成功"),
    CLOSED(2, "已关闭");

    private final Integer code;
    private final String name;
}
