package com.iyzico.ozonosfer.infrastructure.service;

import com.google.common.collect.Iterables;
import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import com.iyzico.ozonosfer.domain.model.RateLimitRequest;
import com.iyzico.ozonosfer.domain.model.RateLimitWindowSize;
import com.iyzico.ozonosfer.domain.service.RateLimiterService;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Basic redis rate limiter service implemented via https://redislabs.com/redis-best-practices/basic-rate-limiting/
 */
@Service
@ConditionalOnProperty(prefix = "ozonosfer.implementation", name = "redis", matchIfMissing = true)
public class RedisBasicRateLimiterService implements RateLimiterService {

    private static final String DELIMITER = ":";
    private static final String KEY_PREFIX_SECOND = "ozon:s:";
    private static final String KEY_PREFIX_MINUTE = "ozon:m:";
    private static final String KEY_PREFIX_HOUR = "ozon:h:";
    private static final double DELTA = 1.0;

    private RedisTemplate<String, String> redisTemplate;

    public RedisBasicRateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void rateLimit(RateLimitRequest request) {
        Long count = retrieveCount(request);
        if (rateLimitExceeded(request.getLimit(), count)) {
            throw new RateLimitedException(request);
        } else {
            incrementCount(request);
        }
    }

    private boolean rateLimitExceeded(Long limit, Long count) {
        return count >= limit;
    }

    private Long retrieveCount(RateLimitRequest request) {
        String key = retrieveKeyAndTimeout(request).v1;
        return redisTemplate.execute(new SessionCallback<Long>() {
            @Override
            public <K, V> Long execute(RedisOperations<K, V> operations) {
                String value = redisTemplate.opsForValue().get(key);
                return Optional.ofNullable(value)
                        .map(Long::parseLong)
                        .orElse(1L);
            }
        });
    }

    private Boolean incrementCount(RateLimitRequest request) {
        String key = retrieveKeyAndTimeout(request).v1;
        Integer timeout = retrieveKeyAndTimeout(request).v2;
        TimeUnit timeUnit = retrieveKeyAndTimeout(request).v3;
        return redisTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            public <K, V> Boolean execute(RedisOperations<K, V> operations) {
                redisTemplate.multi();
                redisTemplate.opsForValue().increment(key, DELTA);
                redisTemplate.expire(key, timeout, timeUnit);
                List<Object> objectList = operations.exec();
                return (Boolean) Iterables.getLast(objectList);
            }
        });

    }

    private Tuple3<String, Integer, TimeUnit> retrieveKeyAndTimeout(RateLimitRequest request) {
        LocalTime now = LocalTime.now();
        String finalKey = request.getPrefix() + DELIMITER + request.getKey();
        if (RateLimitWindowSize.HOUR.equals(request.getWindowSize())) {
            String key = KEY_PREFIX_HOUR + finalKey + DELIMITER + now.getHour();
            return Tuple.tuple(key, 59, TimeUnit.MINUTES);
        } else if (RateLimitWindowSize.SECOND.equals(request.getWindowSize())) {
            String key = KEY_PREFIX_SECOND + finalKey + DELIMITER + now.getSecond();
            return Tuple.tuple(key, 999, TimeUnit.MILLISECONDS);
        } else {
            String key = KEY_PREFIX_MINUTE + finalKey + DELIMITER + now.getMinute();
            return Tuple.tuple(key, 59, TimeUnit.SECONDS);
        }
    }
}