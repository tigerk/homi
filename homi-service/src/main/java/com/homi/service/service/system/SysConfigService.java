package com.homi.service.service.system;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.dao.entity.Config;
import com.homi.model.dao.mapper.ConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final ConfigMapper configMapper;

    /**
     * 根据key，查询value
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/28 12:27
     *
     * @param key 参数说明
     * @return java.lang.String
     */
    public String getConfigValueByKey(String key) {
        Config config = new Config();
        config.setConfigKey(key);
        Config retConfig = configMapper.selectOne(new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, key));
        if (Objects.nonNull(retConfig)) {
            return retConfig.getConfigValue();
        }
        return CharSequenceUtil.EMPTY;
    }
}
