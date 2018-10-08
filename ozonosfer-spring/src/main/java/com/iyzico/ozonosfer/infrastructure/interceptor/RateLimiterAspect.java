package com.iyzico.ozonosfer.infrastructure.interceptor;

import com.iyzico.ozonosfer.domain.annotation.RateLimit;
import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import com.iyzico.ozonosfer.domain.model.RateLimitRequest;
import com.iyzico.ozonosfer.domain.service.RateLimiterService;
import com.iyzico.ozonosfer.infrastructure.service.KeyEvaluator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimiterAspect {

    private Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);

    private final RateLimiterService rateLimiterService;

    public RateLimiterAspect(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Before("@annotation(rateLimitAnnotation)")
    public void before(JoinPoint joinPoint, RateLimit rateLimitAnnotation) {
        RateLimitRequest rateLimitRequest = new RateLimitRequest();
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object value = KeyEvaluator.evaluateExpression(signature.getParameterNames(), joinPoint.getArgs(), rateLimitAnnotation.key());
            rateLimitRequest.setPrefix(rateLimitAnnotation.prefix());
            rateLimitRequest.setKey(value);
            rateLimitRequest.setLimit(rateLimitAnnotation.limit());
            rateLimitRequest.setWindowSize(rateLimitAnnotation.windowSize());
            rateLimiterService.rateLimit(rateLimitRequest);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        if (e instanceof RateLimitedException) {
            throw (RateLimitedException) e;
        } else {
            logger.warn(e.getMessage(), e);
        }
    }
}