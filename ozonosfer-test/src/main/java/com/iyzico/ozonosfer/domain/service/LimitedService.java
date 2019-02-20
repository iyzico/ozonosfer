package com.iyzico.ozonosfer.domain.service;

import com.iyzico.ozonosfer.domain.annotation.RateLimit;
import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import com.iyzico.ozonosfer.domain.model.Message;
import com.iyzico.ozonosfer.domain.model.RateLimitWindowSize;
import com.iyzico.ozonosfer.domain.request.SampleRequest;
import org.springframework.stereotype.Service;

import static com.iyzico.ozonosfer.domain.model.RateLimitWindowSize.MINUTE;

@Service
public class LimitedService {

    @RateLimit(prefix = "limitedService:getMessage", key = "#request.authenticationId", windowSize = RateLimitWindowSize.MINUTE, limit = 10)
    public Message getMessage(SampleRequest request) throws RateLimitedException {
        return new Message(request.getMessage());
    }

    @RateLimit(prefix = "app:method", key = "#request.authenticationId", windowSize = MINUTE, limit = 10)
    public Integer rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
        return 1;
    }
}
