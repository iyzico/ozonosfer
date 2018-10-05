package com.iyzico.ozonosfer.domain;

public interface RateLimiterService {

    void rateLimit(RateLimitRequest rateLimitRequest);
}