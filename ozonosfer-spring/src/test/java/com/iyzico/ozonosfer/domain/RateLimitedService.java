package com.iyzico.ozonosfer.domain;

import com.iyzico.ozonosfer.domain.annotation.RateLimit;
import org.springframework.stereotype.Service;

import static com.iyzico.ozonosfer.domain.model.RateLimitWindowType.MINUTE;

@Service
public class RateLimitedService {

    @RateLimit(prefix = "app:method", key = "#request.authenticationId", windowType = MINUTE, limit = 10)
    public void rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
    }
}
