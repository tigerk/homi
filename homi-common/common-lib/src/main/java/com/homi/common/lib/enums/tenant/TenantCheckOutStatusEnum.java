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
public enum TenantCheckOutStatusEnum {
    /*
     * 租户退租状态：0=未退租、1=正常退、2=违约退、3=续约退、4=换房退、5=转租退
     */
    UN_CHECK_OUT(0, "未退租"),
    NORMAL_CHECK_OUT(1, "正常退"),
    BREAK_CHECK_OUT(2, "违约退"),
    RENEW_CHECK_OUT(3, "续约退"),
    RELOCATION_CHECK_OUT(4, "换房退"),
    SUBLET_CHECK_OUT(5, "转租退"),
    ;

    private final Integer code;
    private final String name;
}
