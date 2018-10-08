package com.iyzico.ozonosfer.domain.exception;

import com.iyzico.ozonosfer.domain.model.RateLimitRequest;

public class RateLimitedException extends RuntimeException {

    public RateLimitedException(RateLimitRequest request) {
        super(request.toString());
    }
}
