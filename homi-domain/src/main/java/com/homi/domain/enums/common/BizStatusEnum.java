package com.homi.domain.enums.common;


import lombok.Getter;

@Getter
public enum BizStatusEnum {

    /**
     * 正常
     */
    ACTIVE(0),

    /**
     * 禁用
     */
    DISABLED(-1);

    private final int value;

    BizStatusEnum(int value) {
        this.value = value;
    }

}
