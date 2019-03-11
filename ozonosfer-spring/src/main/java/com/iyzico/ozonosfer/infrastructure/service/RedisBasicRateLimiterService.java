package com.iyzico.ozonosfer.infrastructure.service;

import com.google.common.collect.Iterables;
import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import com.iyzico.ozonosfer.domain.model.RateLimitRequest;
import com.iyzico.ozonosfer.domain.model.RateLimitWindowSize;
import com.iyzico.ozonosfer.domain.service.RateLimitTogglingService;
import com.iyzico.ozonosfer.domain.service.RateLimiterService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RedisBasicRateLimiterService implements RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RedisBasicRateLimiterService.class);

    public static final String HYSTRIX_COMMAND_KEY = "ozonosfer-hystrix";
    private static final String DELIMITER = ":";
    private static final String KEY_PREFIX_SECOND = "ozon:s:";
    private static final String KEY_PREFIX_MINUTE = "ozon:m:";
    private static final String KEY_PREFIX_HOUR = "ozon:h:";
    private static final double DELTA = 1.0;

    private RedisTemplate<String, String> redisTemplate;
    private RateLimitTogglingService rateLimitTogglingService;

    public RedisBasicRateLimiterService(RedisTemplate<String, String> redisTemplate,
                                        RateLimitTogglingService rateLimitTogglingService) {
        this.redisTemplate = redisTemplate;
        this.rateLimitTogglingService = rateLimitTogglingService;
    }

    @Override
    @HystrixCommand(
            commandKey = HYSTRIX_COMMAND_KEY,
            ignoreExceptions = RateLimitedException.class,
            fallbackMethod = "rateLimitFallback",
            commandProperties = {@HystrixProperty(
                    name = "execution.isolation.thread.timeoutInMilliseconds",
                    value = "1000"
            ), @HystrixProperty(
                    name = "circuitBreaker.requestVolumeThreshold",
                    value = "5"
            ), @HystrixProperty(
                    name = "metrics.rollingStats.timeInMilliseconds",
                    value = "5000"
            )},
            threadPoolProperties = {@HystrixProperty(
                    name = "coreSize",
                    value = "20")})
    public void rateLimit(RateLimitRequest request) {
        if (rateLimitTogglingService.isRateLimitEnabled(String.valueOf(request.getKey()))) {
            Long count = retrieveCount(request);
            if (rateLimitExceeded(request.getLimit(), count)) {
                logger.warn("The rate limit has been exceeded for key: " + request.getKey());
                throw new RateLimitedException(request);
            } else {
                incrementCount(request);
            }
        }
    }


    public void rateLimitFallback(RateLimitRequest rateLimitRequest, Throwable e) {
        logger.warn("Something is wrong with Rate Limiter. Fallback method executed!", e);
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