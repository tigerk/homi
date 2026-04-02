package com.homi.external.sign.fadada;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/4/2
 */

@Component
@ConfigurationProperties(prefix = "fadada")
@Data
public class FadadaProperties {

    /**
     * 是否启用法大大
     */
    private boolean enabled = true;

    /**
     * 法大大应用ID
     */
    private String appId;

    /**
     * 法大大应用密钥
     */
    private String appSecret;

    /**
     * 接口版本
     */
    private String version = "2.0";

    /**
     * 法大大服务地址
     */
    private String host = "https://testapi11.fadada.com/api";

}
