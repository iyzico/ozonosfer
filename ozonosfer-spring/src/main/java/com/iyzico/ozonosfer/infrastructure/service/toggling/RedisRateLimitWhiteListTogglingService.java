package com.iyzico.ozonosfer.infrastructure.service.toggling;

import com.iyzico.ozonosfer.domain.service.RateLimitTogglingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


public class RedisRateLimitWhiteListTogglingService implements RateLimitTogglingService {

    private static final String OZON_LIST_KEY = "ozon-list";

    private RedisTemplate<String, String> redisTemplate;

    public RedisRateLimitWhiteListTogglingService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isRateLimitEnabled(String val) {
        return redisTemplate.opsForSet().isMember(OZON_LIST_KEY, val);
    }

    @Override
    public boolean isRateLimitDisabled(String val) {
        return !redisTemplate.opsForSet().isMember(OZON_LIST_KEY, val);
    }
}
