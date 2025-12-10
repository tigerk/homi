package com.homi.common.lib.utils;

import com.homi.common.lib.enums.ZoneEnum;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 时间工具类
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 02:01
 */
public class TimeUtils {

    private TimeUtils() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of(ZoneEnum.SHANGHAI.getZone()));
    }
}
