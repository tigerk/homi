package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主账单来源类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主账单来源类型枚举")
public enum OwnerBillSourceTypeEnum {
    OWNER_CONTRACT_HOUSE("OWNER_CONTRACT_HOUSE", "业主合同房源");

    private final String code;
    private final String name;
}
