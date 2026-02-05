package com.homi.common.lib.enums.checkout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退租费用类型枚举
 * 所有费用类型均可由用户自行添加选择
 */
@Getter
@AllArgsConstructor
public enum CheckoutFeeTypeEnum {

    // === 收入（扣款）方向 ===
    RENT(1, "租金", 1),
    DEPOSIT(2, "押金", 1),
    WATER(3, "水费", 1),
    ELECTRIC(4, "电费", 1),
    GAS(5, "燃气费", 1),
    PROPERTY_FEE(6, "物业费", 1),
    CLEANING(7, "清洁费", 1),
    DAMAGE(8, "物品损坏", 1),
    PENALTY(9, "违约金", 1),
    OTHER(10, "其他费用", 1),

    // === 支出（退款）方向 ===
    RENT_REFUND(51, "租金", 2),
    DEPOSIT_REFUND(52, "押金", 2),
    OTHER_REFUND(53, "其他退款", 2);

    private final Integer code;
    private final String name;
    /**
     * 方向：1=收（租客应付/扣款），2=支（退还租客/退款）
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
     * 是否为收入（扣款）类型
     */
    public boolean isIncome() {
        return this.direction == 1;
    }

    /**
     * 是否为支出（退款）类型
     */
    public boolean isExpense() {
        return this.direction == 2;
    }
}
