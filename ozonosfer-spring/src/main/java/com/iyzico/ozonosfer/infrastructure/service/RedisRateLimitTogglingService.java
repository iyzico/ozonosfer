package com.iyzico.ozonosfer.infrastructure.service;

import com.iyzico.ozonosfer.domain.service.RateLimitTogglingService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisRateLimitTogglingService implements RateLimitTogglingService {

    private static final String OZON_ENABLED_LIST = "ozon-enabled-list";

    private RedisTemplate<String, String> redisTemplate;

    public RedisRateLimitTogglingService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isRateLimitEnabled(String val) {
        return redisTemplate.opsForSet().isMember(OZON_ENABLED_LIST, val);
    }

    @Override
    public boolean isRateLimitDisabled(String val) {
        return !redisTemplate.opsForSet().isMember(OZON_ENABLED_LIST, val);
    }
}
