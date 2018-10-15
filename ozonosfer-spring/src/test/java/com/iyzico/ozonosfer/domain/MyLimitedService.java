package com.iyzico.ozonosfer.domain;

import com.iyzico.ozonosfer.domain.annotation.RateLimit;
import org.springframework.stereotype.Service;

import static com.iyzico.ozonosfer.domain.model.RateLimitWindowSize.MINUTE;

@Service
public class MyLimitedService {

    @RateLimit(prefix = "app:method", key = "#request.authenticationId", windowSize = MINUTE, limit = 10)
    public Integer rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
        return 1;
    }
}
