package com.homi.service.external.amap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/9/18
 */

@Component
@ConfigurationProperties(prefix = "amap")
@Data
public class AmapConfig {
    private List<String> keys;
}
