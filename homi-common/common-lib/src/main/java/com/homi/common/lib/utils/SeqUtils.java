package com.homi.common.lib.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 序列号生成 工具类
 *
 * @author jinhuayun
 * @date 2023/12/13 16:56
 */
public class SeqUtils {
    private SeqUtils() {
    }

    /**
     * 支付订单号
     */
    private static final AtomicLong PAY_ORDER_SEQ = new AtomicLong(0L);
    /**
     * 是否使用MybatisPlus生成分布式ID
     **/
    private static final boolean IS_USE_HUTOOL_ID = true;

    /**
     * 生成支付订单号，格式：yyyyMMddHHmmssSSS + 4位随机数，例如：20231213165600001234
     **/
    public static String genOrderNo() {
        long ip = NetUtil.ipv4ToLong(String.valueOf(NetUtil.getLocalhostStr()));
        long workerId = ip % 32;

        if (IS_USE_HUTOOL_ID) {
            return String.valueOf(IdUtil.getSnowflake(workerId, workerId).nextId());
        }

        return String.format("%s%04d",
            DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
            (int) PAY_ORDER_SEQ.getAndIncrement() % 10000);
    }
}

