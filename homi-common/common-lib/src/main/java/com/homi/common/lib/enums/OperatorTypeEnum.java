package com.homi.common.lib.enums;

import lombok.Getter;

/**
 * 操作人类别
 */
@Getter
public enum OperatorTypeEnum {

    /**
     * 其它
     */
    OTHER(0),

    /**
     * 后台用户
     */
    MANAGE(1);


    private final int value;

    OperatorTypeEnum(int value) {
        this.value = value;
    }

}
