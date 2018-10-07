package com.iyzico.ozonosfer.domain;

import org.springframework.stereotype.Service;

@Service
public class RateLimitedService {

    @RateLimit(key = "#request.authenticationId", seconds = 60, limit = 1000)
    public void rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
    }
}
