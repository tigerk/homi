package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主提现操作类型枚举")
public enum OwnerWithdrawOperateEnum {
    APPROVE("审批通过"),
    REJECT("审批驳回"),
    PAYING("标记打款中"),
    SUCCESS("打款成功"),
    FAIL("打款失败"),
    CANCEL("取消提现");

    private final String name;
}
