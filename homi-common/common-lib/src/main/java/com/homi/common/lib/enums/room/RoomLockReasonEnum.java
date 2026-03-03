package com.homi.common.lib.enums.room;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "锁房原因: 1-永久锁房, 2-指定时间")
public enum RoomLockReasonEnum {

    PERMANENT(1, "永久锁房"),
    SPECIFIED_TIME(2, "指定时间锁房"),
    ;

    private final Integer code;

    private final String name;
}
