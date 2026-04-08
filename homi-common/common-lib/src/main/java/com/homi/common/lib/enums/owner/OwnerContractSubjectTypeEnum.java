package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主合同房源类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主合同房源类型枚举")
public enum OwnerContractSubjectTypeEnum {
    HOUSE("HOUSE", "房源"),
    BUILDING("BUILDING", "楼栋"),
    COMMUNITY("COMMUNITY", "项目");

    private final String code;
    private final String name;
}
