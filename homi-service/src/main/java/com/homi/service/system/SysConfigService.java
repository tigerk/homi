package com.homi.service.system;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.entity.SysConfig;
import com.homi.model.mapper.SysConfigMapper;
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

    private final SysConfigMapper configMapper;

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
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        SysConfig retConfig = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (Objects.nonNull(retConfig)) {
            return retConfig.getConfigValue();
        }
        return CharSequenceUtil.EMPTY;
    }
}
