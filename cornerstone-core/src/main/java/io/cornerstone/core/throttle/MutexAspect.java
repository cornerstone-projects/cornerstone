package io.cornerstone.core.throttle;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.cornerstone.core.Application;
import io.cornerstone.core.aop.BaseAspect;
import io.cornerstone.core.coordination.LockFailedException;
import io.cornerstone.core.coordination.LockService;
import io.cornerstone.core.util.ReflectionUtils;

@Aspect
@Component
public class MutexAspect extends BaseAspect {

	private static final ExpressionParser expressionParser = new SpelExpressionParser();

	@Autowired
	private LockService lockService;

	public MutexAspect() {
		order = -2000;
	}

	@Around("execution(public * *(..)) and @annotation(mutex)")
	public Object control(ProceedingJoinPoint pjp, Mutex mutex) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		String key = mutex.key();
		if (!StringUtils.hasLength(key)) {
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
		StringBuilder sb = new StringBuilder(key);
		Application.current().ifPresent(a -> {
			switch (mutex.scope()) {
			case GLOBAL:
				break;
			case APPLICATION:
				sb.append('-').append(a.getName());
				break;
			case LOCAL:
				sb.append('-').append(a.getInstanceId(true));
				break;
			default:
				break;
			}
		});
		key = sb.toString();
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
