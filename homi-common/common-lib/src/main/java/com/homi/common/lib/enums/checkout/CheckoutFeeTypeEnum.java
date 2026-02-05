package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退租费用类型枚举
 */
@Getter
@AllArgsConstructor
public enum CheckoutFeeTypeEnum {

    UNPAID_RENT(1, "欠缴租金", 1),
    UNPAID_FEE(2, "欠缴杂费", 1),
    UTILITY(3, "水电燃气", 1),
    DAMAGE(4, "物品损坏", 1),
    PENALTY(5, "违约金", 1),
    CLEANING(6, "清洁费", 1),
    OTHER_DEDUCTION(7, "其他扣款", 1),
    RENT_REFUND(8, "租金退还", 2),
    DEPOSIT_REFUND(9, "押金退还", 2),
    OTHER_REFUND(10, "其他退款", 2);

    private final Integer code;
    private final String name;
    /**
     * 方向：1=扣款，2=退款
     */
    private final Integer direction;

    public static CheckoutFeeTypeEnum getByCode(Integer code) {
        if (code == null) return null;
        for (CheckoutFeeTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        CheckoutFeeTypeEnum e = getByCode(code);
        return e != null ? e.getName() : "";
    }

    /**
     * 是否为扣款类型
     */
    public boolean isDeduction() {
        return this.direction == 1;
    }

    /**
     * 是否为退款类型
     */
    public boolean isRefund() {
        return this.direction == 2;
    }
}
