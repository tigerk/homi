package com.homi.common.lib.enums.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingStatusEnum {
    /**
     * 预定状态：1=预定中，2=已转合同，3=客户违约（没收定金），4=业主违约（退还定金），5=已取消/过期
     */
    BOOKING(1, "预定中", 1),
    CONTRACTED(2, "已转合同", 2),
    TENANT_DEFAULTED(3, "客户违约（没收定金）", 3),
    OWNER_DEFAULTED(4, "业主违约（退还定金）", 4),
    CANCELLED_EXPIRED(5, "已取消/过期", 5);

    private final Integer code;
    private final String name;
    private final Integer sortOrder;


    public static BookingStatusEnum getEnum(Integer bookingStatus) {
        for (BookingStatusEnum e : values()) {
            if (e.getCode().equals(bookingStatus)) {
                return e;
            }
        }
        return null;
    }
}
