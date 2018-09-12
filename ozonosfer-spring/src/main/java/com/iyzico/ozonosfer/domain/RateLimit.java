package com.iyzico.ozonosfer.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {

    /**
     * Key for rate limiting. If the rate limiting is request based per user, the key can be authenticated user id.
     */
    String key();

    /**
     * Size of a time window in seconds. Default value is 60 seconds.
     */
    long seconds() default 60L;

    /**
     * Rate limit count.
     */
    long limit() default 10000L;
}