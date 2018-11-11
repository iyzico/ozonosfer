package com.iyzico.ozonosfer.domain.service;

import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import com.iyzico.ozonosfer.domain.model.Message;
import com.iyzico.ozonosfer.domain.model.RateLimitWindowSize;
import com.iyzico.ozonosfer.domain.request.SampleRequest;
import com.iyzico.ozonosfer.domain.annotation.RateLimit;
import org.springframework.stereotype.Service;

@Service
public class LimitedService {

    @RateLimit(prefix = "limitedService:getMessage", key = "#request.authenticationId", windowSize = RateLimitWindowSize.MINUTE, limit = 10)
    public Message getMessage (SampleRequest request) throws RateLimitedException {
        return new Message(request.getMessage());
    }
}
