package com.iyzico.ozonosfer.infrastructure.service;

import com.google.common.collect.Iterables;
import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import com.iyzico.ozonosfer.domain.model.RateLimitRequest;
import com.iyzico.ozonosfer.domain.model.RateLimitWindowType;
import com.iyzico.ozonosfer.domain.service.RateLimiterService;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(prefix = "ozonosfer.implementation", name = "redis", matchIfMissing = true)
public class RedisRateLimiterService implements RateLimiterService {

    private Logger logger = LoggerFactory.getLogger(RedisRateLimiterService.class);

    private static final String DELIMITER = ":";
    private static final String KEY_PREFIX_SECOND = "ozon:s:";
    private static final String KEY_PREFIX_MINUTE = "ozon:m:";
    private static final String KEY_PREFIX_HOUR = "ozon:h:";
    private static final double DELTA = 1.0;

    private RedisTemplate<String, String> redisTemplate;

    public RedisRateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void rateLimit(RateLimitRequest request) {
        Long count = retrieve(request);
        if (rateLimitExceeded(request, count)) {
            throw new RateLimitedException(request);
        } else {
            increment(request);
        }
    }

    private boolean rateLimitExceeded(RateLimitRequest request, Long count) {
        try {
            return count != null && count >= request.getLimit();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return false;
        }
    }

    private Long retrieve(RateLimitRequest request) {
        try {
            String key = retrieveKeyAndTimeout(request).v1;
            return redisTemplate.execute(new SessionCallback<Long>() {
                @Override
                public <K, V> Long execute(RedisOperations<K, V> operations) {
                    String value = redisTemplate.opsForValue().get(key);
                    if (value == null) {
                        return 1L;
                    } else {
                        return Long.parseLong(value);
                    }
                }
            });
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    private Boolean increment(RateLimitRequest request) {
        try {
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
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return false;
        }
    }

    private Tuple3<String, Integer, TimeUnit> retrieveKeyAndTimeout(RateLimitRequest request) {
        LocalTime now = LocalTime.now();
        String finalKey = request.getPrefix() + DELIMITER + request.getKey();
        if (RateLimitWindowType.HOUR.equals(request.getWindowType())) {
            String key = KEY_PREFIX_HOUR + finalKey + DELIMITER + now.getHour();
            return Tuple.tuple(key, 59, TimeUnit.MINUTES);
        } else if (RateLimitWindowType.SECOND.equals(request.getWindowType())) {
            String key = KEY_PREFIX_SECOND + finalKey + DELIMITER + now.getSecond();
            return Tuple.tuple(key, 999, TimeUnit.MILLISECONDS);
        } else {
            String key = KEY_PREFIX_MINUTE + finalKey + DELIMITER + now.getMinute();
            return Tuple.tuple(key, 59, TimeUnit.SECONDS);
        }
    }
}