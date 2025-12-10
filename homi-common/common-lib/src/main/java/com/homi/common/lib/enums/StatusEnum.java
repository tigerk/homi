package com.homi.common.lib.enums;


import lombok.Getter;

@Getter
public enum StatusEnum {

    /**
     * 正常，生效中
     */
    ACTIVE(1),

    /**
     * 停用
     */
    DISABLED(0);

    private final int value;

    StatusEnum(int value) {
        this.value = value;
    }

}
