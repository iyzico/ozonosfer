package com.iyzico.ozonosfer.domain.annotation;

import com.iyzico.ozonosfer.domain.model.RateLimitWindowSize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {

    /**
     * Key prefix for rate limiting. Use application name as convention.
     */
    String prefix();

    /**
     * Key for rate limiting. If the rate limiting is request based per user, the key can be authenticated user id.
     */
    String key();

    /**
     * Window size of rate limiter. Default is MINUTE.
     */
    RateLimitWindowSize windowSize() default RateLimitWindowSize.MINUTE;

    /**
     * Rate limit count.
     */
    long limit() default 10000L;
}