package com.iyzico.ozonosfer.infrastructure.service.toggling;

import com.iyzico.ozonosfer.domain.service.RateLimitTogglingService;
import org.springframework.data.redis.core.RedisTemplate;


public class RedisRateLimitBlackListTogglingService implements RateLimitTogglingService {

    private static final String OZON_LIST_KEY = "ozon-list";

    private RedisTemplate<String, String> redisTemplate;

    public RedisRateLimitBlackListTogglingService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isRateLimitEnabled(String val) {
        return !redisTemplate.opsForSet().isMember(OZON_LIST_KEY, val);
    }

    @Override
    public boolean isRateLimitDisabled(String val) {
        return redisTemplate.opsForSet().isMember(OZON_LIST_KEY, val);
    }
}
