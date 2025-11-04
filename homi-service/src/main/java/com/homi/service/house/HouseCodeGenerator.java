package com.homi.service.house;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/4
 */

@Service
@RequiredArgsConstructor
public class HouseCodeGenerator {
    private final StringRedisTemplate redisTemplate;

    public String generate(String companyCode) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String key = "house:count:" + date + ":" + companyCode;
        Long count = redisTemplate.opsForValue().increment(key);

        // 一天后过期
        redisTemplate.expire(key, 86400, TimeUnit.SECONDS);

        String seq = String.format("%04d", count);
        return companyCode.toUpperCase() + date + seq;
    }
}
