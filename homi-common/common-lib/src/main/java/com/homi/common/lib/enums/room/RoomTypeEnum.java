package com.homi.common.lib.enums.room;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomTypeEnum {
    /**
     * { label: "主卧", value: "1" },
     * { label: "次卧", value: "2" },
     * { label: "隔断", value: "3" },
     * { label: "厅隔", value: "4" },
     * { label: "单间配套", value: "5" },
     * { label: "单间", value: "6" },
     * { label: "双人间", value: "7" },
     * { label: "多人间", value: "8" }
     */
    MASTER_BEDROOM(1, "主卧", "#FF2800"),
    SECOND_BEDROOM(2, "次卧", "#52C41A"),
    BREAK(3, "隔断", "#EAA212"),
    HALF_LIVING(4, "厅隔", "#4B50AD"),
    SINGLE_SUIT(5, "单间配套", "#DBDBDB"),
    SINGLE(6, "单间", "#DBDBDB"),
    DOUBLE(7, "双人间", "#DBDBDB"),
    MULTI(8, "多人间", "#DBDBDB"),
    ;

    private final Integer code;

    private final String name;

    private final String color;
}
