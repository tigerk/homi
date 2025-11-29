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
public enum UserTypeEnum {
    /**
     * 公司管理员
     * 公司管理员只能获取公司绑定套餐的权限
     */
    COMPANY_ADMIN(20, "公司管理员"),

    /**
     * 可以获取配置的角色权限
     */
    COMPANY_USER(21, "公司用户");

    private final int type;
    private final String typeStr;

}
