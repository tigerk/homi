package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主主体类型枚举")
public enum OwnerTypeEnum {
    PERSONAL(0, "个人业主"),
    COMPANY(1, "企业业主");

    private final Integer code;
    private final String name;
}
