package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "包租业主应付单付款状态枚举")
public enum OwnerPayableBillPaymentStatusEnum {
    UNPAID(0, "未付款"),
    PART_PAID(1, "部分付款"),
    PAID(2, "已付款");

    private final Integer code;
    private final String name;
}
