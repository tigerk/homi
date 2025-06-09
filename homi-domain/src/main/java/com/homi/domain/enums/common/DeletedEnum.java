package com.homi.domain.enums.common;

public enum DeletedEnum {

    /**
     * 未删除
     */
    NOT_DELETED(0),

    /**
     * 已删除
     */
    DELETED(1);

    private final int value;

    DeletedEnum(int value) {
        this.value = value;
    }
}
