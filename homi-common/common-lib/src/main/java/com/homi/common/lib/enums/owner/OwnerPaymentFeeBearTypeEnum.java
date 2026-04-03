package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主支付手续费承担方式枚举")
public enum OwnerPaymentFeeBearTypeEnum {
    PLATFORM_ALL("PLATFORM_ALL", "公司承担100%"),
    OWNER_ALL("OWNER_ALL", "业主承担100%"),
    BY_INCOME_SHARE("BY_INCOME_SHARE", "各自承担自己所得");

    private final String code;
    private final String name;
}
