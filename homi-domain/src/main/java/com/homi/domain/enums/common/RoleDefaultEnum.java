package com.homi.domain.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 初始角色枚举
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:28
 */

@Getter
@AllArgsConstructor
public enum RoleDefaultEnum {

    /**
     * 超级管理员
     */
    PLATFORM_SUPER_ADMIN(1L, "公司管理员", "platform-admin"),
    /**
     * 管理员
     */
    COMPANY_ADMIN(2L, "公司管理员", "company-admin"),
    /**
     * 普通用户
     */
    USER(3L, "普通用户", "user");

    /**
     * 角色id
     */
    private final Long id;

    /**
     * 角色名称
     */
    private final String roleName;

    /**
     * 角色编码
     */
    private final String roleCode;

    /**
     * 根据角色ID获取角色枚举。
     *
     * @param id 角色ID
     * @return 对应的角色枚举，如果没有匹配则返回null
     */
    public static RoleDefaultEnum fromValue(Long id) {
        if (id == null) {
            return null;
        }
        for (RoleDefaultEnum role : RoleDefaultEnum.values()) {
            if (role.getId().equals(id)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 根据角色编码获取角色枚举。
     *
     * @param code 角色编码
     * @return 对应的角色枚举，如果没有匹配则返回null
     */
    public static RoleDefaultEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (RoleDefaultEnum role : RoleDefaultEnum.values()) {
            if (role.getRoleCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
