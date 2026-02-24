package com.homi.common.lib.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance)
            .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
}
