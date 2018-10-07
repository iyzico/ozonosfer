package com.iyzico.ozonosfer.domain.service;

import com.iyzico.ozonosfer.domain.model.RateLimitRequest;

public interface RateLimiterService {

    void rateLimit(RateLimitRequest rateLimitRequest);
}