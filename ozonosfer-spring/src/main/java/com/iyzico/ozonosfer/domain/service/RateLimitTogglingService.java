package com.iyzico.ozonosfer.domain.service;

public interface RateLimitTogglingService {

    boolean isRateLimitEnabled(String val);

    boolean isRateLimitDisabled(String val);
}
