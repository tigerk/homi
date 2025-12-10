package com.homi.common.lib.enums.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:26
 */
@AllArgsConstructor
@Getter
public enum PlatformUserTypeEnum {
    /**
     * 平台超级管理员，平台管理员直接获取所有权限
     */
    SUPER_USER(10, "超级管理员"),

    /**
     * 可以获取配置的角色权限
     */
    REGULAR_USER(20, "普通用户");

    private final int type;
    private final String typeStr;

}
