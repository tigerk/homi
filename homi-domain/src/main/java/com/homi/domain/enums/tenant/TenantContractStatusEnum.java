package com.homi.domain.enums.tenant;

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
public enum TenantContractStatusEnum {
    /**
     * 合同状态：0=未生效，1=生效中，2=已退租，3=已逾期，4=已作废
     */
    UN_EFFECTIVE(0, "待签字"),
    EFFECTIVE(1, "在租中"),
    TERMINATED(2, "已退租"),
    CANCELLED(-1, "已作废");

    private final Integer code;
    private final String name;
}
