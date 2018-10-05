package com.iyzico.ozonosfer.infrastructure.service;

import com.iyzico.ozonosfer.domain.RateLimitRequest;
import com.iyzico.ozonosfer.domain.RateLimiterService;
import org.springframework.stereotype.Service;

@Service
public class RedisRateLimiterService implements RateLimiterService {

    @Override
    public void rateLimit(RateLimitRequest rateLimitRequest) {
        System.out.println("rate limiter service called with " + rateLimitRequest.toString());
    }
}