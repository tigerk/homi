package com.homi.common.lib.config;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/2/2
 */

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Modulith 配置
 * <p>
 * 只需要引入依赖，Spring Boot 自动配置会处理一切
 */
@Configuration
@EnableAsync  // 如果需要异步处理事件
public class ModulithConfiguration {
    // 不需要额外配置，Spring Modulith 会自动：
    // 1. 创建 event_publication 表
    // 2. 启用事件持久化
    // 3. 配置事务性事件监听
}
