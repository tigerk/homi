package com.homi.common.lib.enums;

import lombok.Getter;

@Getter
public enum TrialApplicationStatusEnum {
    PENDING(0, "申请中"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String name;

    TrialApplicationStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
