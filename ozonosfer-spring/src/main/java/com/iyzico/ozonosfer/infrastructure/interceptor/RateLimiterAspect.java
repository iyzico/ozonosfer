package com.iyzico.ozonosfer.infrastructure.interceptor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class RateLimiterAspect {

    @Before("@annotation(com.iyzico.ozonosfer.domain.RateLimit)")
    public void before(JoinPoint joinPoint) {
        //TODO call rate limiter service
        System.err.println("rate limiter called!!!");
    }
}
