package com.iyzico.ozonosfer.infrastructure.interceptor;

import com.iyzico.ozonosfer.domain.RateLimit;
import com.iyzico.ozonosfer.domain.service.RateLimiterService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimiterAspect {

    private final RateLimiterService rateLimiterService;

    public RateLimiterAspect(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Before("@annotation(com.iyzico.ozonosfer.domain.RateLimit)")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimitAnnotation = signature.getMethod().getAnnotation(RateLimit.class);
        Object value = getValueByKey(signature.getParameterNames(), joinPoint.getArgs(), rateLimitAnnotation.key());
        rateLimiterService.rateLimit(value, rateLimitAnnotation.limit(), rateLimitAnnotation.seconds());
    }

    private Object getValueByKey(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context);
    }
}
