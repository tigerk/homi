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
public enum TenantStatusEnum {
    /**
     * 合同状态：0=待签字，1=在租中，2=已退租，3=已作废
     */
    TO_SIGN(0, "待签字", "#FF2800", 0),
    EFFECTIVE(1, "在租中", "#52C41A", 1),
    TERMINATED(2, "已退租", "#EAA212", 2),
    CANCELLED(-1, "已作废", "#DBDBDB", 3);

    private final Integer code;
    private final String name;
    private final String color;

    private final Integer sortOrder;
}
