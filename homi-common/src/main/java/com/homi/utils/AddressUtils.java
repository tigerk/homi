package com.homi.utils;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取地址类
 *
 * @author ruoyi
 */
public class AddressUtils {
    private AddressUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP地址查询
    public static final String IP_URL = "http://ip-api.com/json/%s?lang=zh-CN";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        // 内网不查询
        if (Ipv4Util.isInnerIP(ip)) {
            return "内网IP";
        }

        try {
            String rspStr = HttpUtil.get(String.format(IP_URL, ip), 2000);
            System.out.println(rspStr);
            if (StringUtils.isEmpty(rspStr)) {
                log.error("获取地理位置异常 {}", ip);
                return UNKNOWN;
            }
            JSONObject obj = JSON.parseObject(rspStr);
            String region = obj.getString("regionName");
            String city = obj.getString("city");
            if (StringUtils.isEmpty(region) || StringUtils.isEmpty(city)) {
                return obj.getString("addr");
            }
            return String.format("%s %s", region, city);
        } catch (Exception e) {
            log.error("获取地理位置异常 {}", ip, e);
        }

        return UNKNOWN;
    }

    public static void main(String[] args) {
        String realAddressByIP = AddressUtils.getRealAddressByIP("36.106.206.40");
        System.out.println(realAddressByIP);
    }
}
