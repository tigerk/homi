package com.homi.common.lib.config;

import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.json.PackageVersion;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/16
 */

@Configuration
public class JacksonConfig {
    static {
        // 在静态代码块中全局替换类型映射
        // 这一步是关键：强制将 Long 类型在文档中显示为 String
        SpringDocUtils.getConfig().replaceWithSchema(Long.class, new StringSchema());
        SpringDocUtils.getConfig().replaceWithSchema(long.class, new StringSchema());
    }

    @Bean
    public SimpleModule longToStringJacksonModule() {
        return new SimpleModule("long-to-string", PackageVersion.VERSION)
            .addSerializer(Long.class, ToStringSerializer.instance)
            .addSerializer(Long.TYPE, ToStringSerializer.instance);
    }
}
