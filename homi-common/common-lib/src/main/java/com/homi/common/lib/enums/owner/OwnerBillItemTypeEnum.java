package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主账单明细类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主账单明细类型枚举")
public enum OwnerBillItemTypeEnum {
    RENT("RENT", "租金"),
    DEPOSIT("DEPOSIT", "押金"),
    OTHER_FEE("OTHER_FEE", "其他费用"),
    MANAGEMENT_FEE("MANAGEMENT_FEE", "管理费");

    private final String code;
    private final String name;
}
