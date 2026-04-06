package com.homi.saas.web.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * ShedLock 配置
 * <p>
 * 使用 Redis 作为分布式锁存储，避免多实例部署时重复执行定时任务。
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class ShedLockConfig {

    /**
     * 定时任务分布式锁提供者
     *
     * @param connectionFactory Redis 连接工厂
     * @param applicationName   应用名称
     * @return LockProvider
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory,
                                     @Value("${spring.application.name:homi-saas}") String applicationName) {
        return new RedisLockProvider(connectionFactory, applicationName);
    }
}
