package com.homi.common.lib.enums.house;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DecorationTypeEnum {
    /**
     * 装修类型：1=豪华装，2=简装，3=精装，4=毛坯，5=清水，6=简约，7=未装修
     */
    LUXURY(1, "豪华装"),
    SIMPLE(2, "简装"),
    DETAILED(3, "精装"),
    RAW(4, "毛坯"),
    WATER(5, "清水"),
    SIMPLEST(6, "简约"),
    UNDECORATED(7, "未装修");

    private final Integer code;

    private final String name;
}
