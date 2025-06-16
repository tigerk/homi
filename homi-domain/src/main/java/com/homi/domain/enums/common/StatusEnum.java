package com.homi.domain.enums.common;


import lombok.Getter;

@Getter
public enum StatusEnum {

    /**
     * 正常，生效中
     */
    ACTIVE(0),

    /**
     * 禁用
     */
    DISABLED(-1);

    private final int value;

    StatusEnum(int value) {
        this.value = value;
    }

}
