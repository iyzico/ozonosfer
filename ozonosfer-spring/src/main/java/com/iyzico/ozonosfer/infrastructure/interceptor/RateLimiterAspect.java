package com.iyzico.ozonosfer.infrastructure.interceptor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class RateLimiterAspect {

    @Pointcut("within(@com.iyzico.ozonosfer.domain.RateLimit *)")
    public void methodsAnnotatedWithRateLimit() {
    }

    @Pointcut("execution(public * *(..))")
    public void publicMethods() {
    }

    @Before("methodsAnnotatedWithRateLimit() && publicMethods()")
    public void before(JoinPoint joinPoint) {
        //Advice
        System.err.println("before annotation!!!");
    }
}
