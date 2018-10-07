package com.iyzico.ozonosfer.infrastructure.service;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.stream.IntStream;

public class KeyEvaluator {

    public static Object evaluateExpression(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();

        IntStream.range(0, parameterNames.length)
                .forEach(i -> context.setVariable(parameterNames[i], args[i]));

        return parser.parseExpression(key).getValue(context);
    }
}