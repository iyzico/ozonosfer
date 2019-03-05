package com.iyzico.ozonosfer.infrastructure.service;

import com.iyzico.ozonosfer.domain.service.ListService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisWhiteListService implements ListService {

    private static final String OZONOSFER_WHITE_LIST = "ozonosfer-white-list";

    private RedisTemplate<String, String> redisTemplate;

    public RedisWhiteListService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isLimited(String val) {
        return !redisTemplate.opsForSet().isMember(OZONOSFER_WHITE_LIST, val);
    }

    @Override
    public boolean isNotLimited(String val) {
        return redisTemplate.opsForSet().isMember(OZONOSFER_WHITE_LIST, val);
    }
}
