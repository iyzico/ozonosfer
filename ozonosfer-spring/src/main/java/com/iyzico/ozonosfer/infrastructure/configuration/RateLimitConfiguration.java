package com.iyzico.ozonosfer.infrastructure.configuration;

import com.iyzico.ozonosfer.domain.RateLimiterService;
import com.iyzico.ozonosfer.infrastructure.service.RedisRateLimiterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfiguration {

    @Bean
    public RateLimiterService rateLimiterService() {
        //TODO Currently returns RedisRateLimiterService but in the long term it'll be decided according to configuration.
        return new RedisRateLimiterService();
    }
}