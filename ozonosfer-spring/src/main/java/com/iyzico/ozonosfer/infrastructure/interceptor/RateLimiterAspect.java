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

import java.util.stream.IntStream;

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
        Object value = getValueByKey(signature.getParameterNames(), joinPoint.getArgs(), rateLimitAnnotation.key());
        rateLimiterService.rateLimit(value, rateLimitAnnotation.limit(), rateLimitAnnotation.seconds());
    }

    private Object getValueByKey(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();

        IntStream.range(0, parameterNames.length)
                .forEach(i -> context.setVariable(parameterNames[i], args[i]));

        return parser.parseExpression(key).getValue(context);
    }
}
