package com.iyzico.ozonosfer.infrastructure.interceptor;

import com.iyzico.ozonosfer.domain.RateLimit;
import com.iyzico.ozonosfer.domain.RateLimitRequest;
import com.iyzico.ozonosfer.domain.RateLimiterService;
import com.iyzico.ozonosfer.infrastructure.service.KeyEvaluator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimiterAspect {

    private final RateLimiterService rateLimiterService;

    public RateLimiterAspect(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Before("@annotation(rateLimitAnnotation)")
    public void before(JoinPoint joinPoint, RateLimit rateLimitAnnotation) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object value = KeyEvaluator.evaluateExpression(signature.getParameterNames(), joinPoint.getArgs(), rateLimitAnnotation.key());

        RateLimitRequest rateLimitRequest = new RateLimitRequest();
        rateLimitRequest.setKey(value);
        rateLimitRequest.setLimit(rateLimitAnnotation.limit());
        rateLimitRequest.setSeconds(rateLimitAnnotation.seconds());

        rateLimiterService.rateLimit(rateLimitRequest);
    }
}