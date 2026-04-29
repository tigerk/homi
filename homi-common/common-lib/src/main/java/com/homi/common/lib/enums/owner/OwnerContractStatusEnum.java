package com.homi.common.lib.enums.owner;

import cn.hutool.core.util.EnumUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主合同状态枚举")
public enum OwnerContractStatusEnum {
    PENDING_APPROVAL(0, "待审核"),
    PENDING_SIGN(1, "待签字"),
    SIGNED(2, "已签字"),
    CHECKED_OUT(3, "已退房"),
    VOIDED(-1, "已作废");

    private final Integer code;
    private final String name;

    public static OwnerContractStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return EnumUtil.getBy(OwnerContractStatusEnum::getCode, code);
    }

    public static OwnerContractStatusEnum fromSignStatus(OwnerSignStatusEnum signStatus) {
        return OwnerSignStatusEnum.SIGNED.equals(signStatus) ? SIGNED : PENDING_SIGN;
    }
}
