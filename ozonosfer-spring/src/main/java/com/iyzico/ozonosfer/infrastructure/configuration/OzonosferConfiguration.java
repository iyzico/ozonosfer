package com.iyzico.ozonosfer.infrastructure.configuration;

import com.iyzico.ozonosfer.domain.service.RateLimitTogglingService;
import com.iyzico.ozonosfer.infrastructure.service.toggling.RedisRateLimitBlackListTogglingService;
import com.iyzico.ozonosfer.infrastructure.service.toggling.RedisRateLimitWhiteListTogglingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ComponentScan(basePackages = {"com.iyzico.ozonosfer"})
public class OzonosferConfiguration {

    private RedisTemplate<String, String> redisTemplate;

    public OzonosferConfiguration(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    @ConditionalOnProperty(value = "ozonosfer.toggling", havingValue = "white-list")
    public RateLimitTogglingService redisRateLimitWhiteListTogglingService() {
        return new RedisRateLimitWhiteListTogglingService(redisTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = "ozonosfer", name = "toggling", havingValue = "black-list", matchIfMissing = true)
    public RateLimitTogglingService redisRateLimitBlackListTogglingService() {
        return new RedisRateLimitBlackListTogglingService(redisTemplate);
    }
}
