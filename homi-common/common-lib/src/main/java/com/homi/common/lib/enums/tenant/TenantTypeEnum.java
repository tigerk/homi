package com.homi.common.lib.enums.tenant;

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
public enum TenantTypeEnum {
    /**
     * 租户类型：0=个人租户，1=企业租户
     */
    PERSONAL(0, "个人租户"),
    ENTERPRISE(1, "企业租户");

    private final Integer code;
    private final String name;
}
