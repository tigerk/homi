package com.homi.common.lib.enums.lease;

import cn.hutool.core.util.EnumUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "租客签约合同文档状态枚举")
public enum LeaseContractDocStatusEnum {
    ACTIVE(1, "有效"),
    VOIDED(-1, "已作废");

    private final Integer code;
    private final String name;

    public static LeaseContractDocStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return EnumUtil.getBy(LeaseContractDocStatusEnum::getCode, code);
    }
}
