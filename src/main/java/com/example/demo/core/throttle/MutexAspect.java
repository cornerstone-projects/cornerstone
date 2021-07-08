package com.example.demo.core.throttle;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.demo.core.coordination.LockFailedException;
import com.example.demo.core.coordination.LockService;
import com.example.demo.core.util.ReflectionUtils;

@Aspect
@Component
@Order(-2000)
public class MutexAspect {

	private static final ExpressionParser expressionParser = new SpelExpressionParser();

	@Autowired
	private LockService lockService;

	@Around("execution(public * *(..)) and @annotation(mutex)")
	public Object control(ProceedingJoinPoint pjp, Mutex mutex) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		String key = mutex.value();
		if (!StringUtils.hasLength(key)) {
			key = ReflectionUtils.stringify(method);
		} else {
			String[] paramNames = ReflectionUtils.getParameterNames(method);
			Object[] paramValues = pjp.getArgs();
			StandardEvaluationContext context = new StandardEvaluationContext();
			if (paramNames != null) {
				for (int i = 0; i < paramNames.length; i++) {
					context.setVariable(paramNames[i], paramValues[i]);
				}
			}
			key = String.valueOf(
					expressionParser.parseExpression(key, ParserContext.TEMPLATE_EXPRESSION).getValue(context));
		}

		if (lockService.tryLock(key)) {
			try {
				return pjp.proceed();
			} finally {
				lockService.unlock(key);
			}
		} else {
			throw new LockFailedException(key);
		}
	}

}
