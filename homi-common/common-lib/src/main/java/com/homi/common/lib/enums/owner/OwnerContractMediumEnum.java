package com.homi.common.lib.enums.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "业主合同介质枚举")
public enum OwnerContractMediumEnum {
    ELECTRONIC("ELECTRONIC", "电子合同"),
    PAPER("PAPER", "纸质合同");

    private final String code;
    private final String name;
}
