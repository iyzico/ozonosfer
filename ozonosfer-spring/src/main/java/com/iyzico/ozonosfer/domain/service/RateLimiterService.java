package com.iyzico.ozonosfer.domain.service;

import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    public void rateLimit(Object key, long limit, long seconds) {
        System.out.println("rate limiter service called with key: " + key + " limit: " + limit + " seconds: " + seconds);
    }
}
