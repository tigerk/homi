package com.homi.common.lib.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON 工具类
 */
@Slf4j
public class JsonUtils {

    public static boolean isJson(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return false;
        }

        return JSONUtil.isTypeJSON(text);
    }

}
