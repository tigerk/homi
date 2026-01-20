package com.homi.common.lib.enums.tenant;

import cn.hutool.core.collection.ListUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

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
     * 租客状态：0=待签字，1=在租中，2=已退租，-1=已作废
     */
    TO_SIGN(0, "待签字", "#FF2800", 0),
    EFFECTIVE(1, "在租中", "#52C41A", 1),
    TERMINATED(2, "已退租", "#EAA212", 2),
    CANCELLED(-1, "已作废", "#DBDBDB", 3);

    private final Integer code;
    private final String name;
    private final String color;

    private final Integer sortOrder;

    /**
     * 获取有效状态
     */
    public static List<Integer> getValidStatus() {
        return ListUtil.of(EFFECTIVE.getCode(), TO_SIGN.getCode());
    }
}
