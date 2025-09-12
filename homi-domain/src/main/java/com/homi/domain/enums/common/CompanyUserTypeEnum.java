package com.homi.domain.enums.common;

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
public enum CompanyUserTypeEnum {
    /**
     * 可以获取配置的角色权限
     */
    COMPANY_ADMIN(21, "公司管理员"),
    COMPANY_DEPUTY(25, "公司副管理员"),
    COMPANY_USER(29, "公司普通用户");

    private final int type;
    private final String typeStr;

}
