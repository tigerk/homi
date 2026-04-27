package com.homi.common.lib.enums.biz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业务操作类型枚举")
public enum BizOperateTypeEnum {
    CREATE("CREATE", "新增"),
    SAVE("SAVE", "保存"),
    UPDATE("UPDATE", "修改"),
    CANCEL("CANCEL", "作废"),
    PAY("PAY", "付款");

    private final String code;
    private final String name;
}
