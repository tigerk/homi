package com.homi.service.service.house;

import com.homi.common.lib.redis.RedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        String key = RedisKey.HOUSE_CODE_COUNTER.format(date, companyCode);
        Long count = redisTemplate.opsForValue().increment(key);
        // 一天后过期
        redisTemplate.expire(key, RedisKey.HOUSE_CODE_COUNTER.getTimeout(), RedisKey.HOUSE_CODE_COUNTER.getUnit());

        String seq = String.format("%04d", count);
        return companyCode.toUpperCase() + date + seq;
    }
}
