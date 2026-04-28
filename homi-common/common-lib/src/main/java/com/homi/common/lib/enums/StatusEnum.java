package com.homi.common.lib.enums;


import cn.hutool.core.util.EnumUtil;
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

    private final Integer value;

    StatusEnum(Integer value) {
        this.value = value;
    }

    public static StatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return EnumUtil.getBy(StatusEnum::getValue, value);
    }
}
