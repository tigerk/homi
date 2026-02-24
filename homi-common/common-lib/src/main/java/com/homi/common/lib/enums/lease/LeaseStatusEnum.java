package com.homi.common.lib.enums.lease;

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
public enum LeaseStatusEnum {
    /**
     * 租客状态：0=待审批，1=待签字，2=在租中，3=已退租，-1=已作废
     */
    PENDING_APPROVAL(0, "待审批", "#FF2800", 0),
    TO_SIGN(1, "待签字", "#FF2800", 1),
    EFFECTIVE(2, "在租中", "#52C41A", 2),
    TERMINATED(3, "已退租", "#EAA212", 3),
    CANCELLED(-1, "已作废", "#DBDBDB", 4);

    private final Integer code;
    private final String name;
    private final String color;

    private final Integer sortOrder;

    /**
     * 获取有效状态
     */
    public static List<Integer> getValidStatus() {
        return ListUtil.of(EFFECTIVE.getCode(), TO_SIGN.getCode(), PENDING_APPROVAL.getCode());
    }
}
