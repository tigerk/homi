package com.homi.common.lib.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/10
 */

@Getter
@AllArgsConstructor
public enum IdTypeEnum {
    /*
     * 证件类型：0=身份证，1=护照，2=港澳通行证，3=台胞证
     */
    ID_CARD(0, "身份证"),
    PASSPORT(1, "护照"),
    HONGKONG_MACAO(2, "港澳通行证"),
    TAIWAN(3, "台胞证");

    private final Integer code;
    private final String name;
}
