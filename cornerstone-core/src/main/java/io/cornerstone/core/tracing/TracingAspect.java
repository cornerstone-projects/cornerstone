package io.cornerstone.core.tracing;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.cornerstone.core.Application;
import io.cornerstone.core.aop.BaseAspect;
import io.cornerstone.core.util.ReflectionUtils;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Aspect
public class TracingAspect extends BaseAspect {

	private static final ExpressionParser expressionParser = new SpelExpressionParser();

	private static final String TAG_NAME_CALLSITE = "callsite";

	private static final String TAG_NAME_PREFIX_PARAM = "param.";

	private static final String TAG_NAME_TX_READONLY = "tx.readonly";

	public TracingAspect() {
		this.order = Ordered.HIGHEST_PRECEDENCE + 1;
	}

	@Around("execution(public * *(..)) and @annotation(traced)")
	public Object trace(ProceedingJoinPoint pjp, Traced traced) throws Throwable {
		Tracer tracer = GlobalTracer.get();
		if (traced.withActiveSpanOnly() && (tracer.activeSpan() == null)) {
			return pjp.proceed();
		}
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		String operationName = traced.operationName();
		if (operationName.isEmpty()) {
			operationName = ReflectionUtils.stringify(method);
		}
		Span span = tracer.buildSpan(operationName).start();
		Object result = null;
		try (Scope scope = tracer.activateSpan(span)) {
			String[] paramNames = ReflectionUtils.getParameterNames(method);
			Object[] paramValues = pjp.getArgs();
			StandardEvaluationContext context = new StandardEvaluationContext();
			if (paramNames != null) {
				for (int i = 0; i < paramNames.length; i++) {
					context.setVariable(paramNames[i], paramValues[i]);
				}
			}
			for (Tag tag : traced.tags()) {
				span.setTag(tag.name(),
						String.valueOf(expressionParser.parseExpression(tag.value()).getValue(context)));
			}
			if (isDebug()) {
				span.setTag(TAG_NAME_CALLSITE, getCallSite(method, pjp.getTarget()));
				if (paramNames != null) {
					for (int i = 0; i < paramNames.length; i++) {
						span.setTag(TAG_NAME_PREFIX_PARAM + paramNames[i], String.valueOf(paramValues[i]));
					}
				}
			}
			result = pjp.proceed();
			return result;
		}
		catch (Exception ex) {
			Tracing.logError(ex);
			throw ex;
		}
		finally {
			span.finish();
		}
	}

	@Around("execution(public * *(..)) and @annotation(transactional) and not @annotation(Traced)")
	public Object traceTransactional(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		List<Serializable> tags = new ArrayList<>();
		tags.add(Tags.COMPONENT.getKey());
		tags.add("tx");
		tags.add(TAG_NAME_TX_READONLY);
		tags.add(transactional.readOnly());
		if (isDebug()) {
			Object target = pjp.getTarget();
			tags.add(TAG_NAME_CALLSITE);
			tags.add(getCallSite(method, target));
			String[] paramNames = ReflectionUtils.getParameterNames(method);
			if (paramNames != null) {
				for (int i = 0; i < paramNames.length; i++) {
					tags.add(TAG_NAME_PREFIX_PARAM + paramNames[i]);
					tags.add(String.valueOf(pjp.getArgs()[i]));
				}
			}
		}
		return Tracing.executeCheckedCallable(ReflectionUtils.stringify(method), pjp::proceed,
				tags.toArray(new Serializable[0]));
	}

	@Around("execution(public * *(..)) and target(org.springframework.data.repository.Repository))")
	public Object traceRepository(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		if (method.isAnnotationPresent(Traced.class) || method.isAnnotationPresent(Transactional.class)) {
			return pjp.proceed();
		}
		List<Serializable> tags = new ArrayList<>();
		tags.add(Tags.COMPONENT.getKey());
		tags.add("tx");
		if (isDebug()) {
			Object target = pjp.getTarget();
			tags.add(TAG_NAME_CALLSITE);
			tags.add(getCallSite(method, target));
			String[] paramNames = ReflectionUtils.getParameterNames(method);
			if (paramNames != null) {
				for (int i = 0; i < paramNames.length; i++) {
					tags.add(TAG_NAME_PREFIX_PARAM + paramNames[i]);
					tags.add(String.valueOf(pjp.getArgs()[i]));
				}
			}
		}
		return Tracing.executeCheckedCallable(ReflectionUtils.stringify(method), pjp::proceed,
				tags.toArray(new Serializable[0]));
	}

	@Around("execution(public * *(..)) and @within(restController)")
	public Object trace(ProceedingJoinPoint pjp, RestController restController) throws Throwable {
		if (!Tracing.isEnabled()) {
			return pjp.proceed();
		}
		RequestMapping requestMapping = AnnotatedElementUtils
			.findMergedAnnotation(((MethodSignature) pjp.getSignature()).getMethod(), RequestMapping.class);
		if (requestMapping == null) {
			return pjp.proceed();
		}
		RequestMapping requestMappingWithClass = AnnotatedElementUtils.findMergedAnnotation(pjp.getTarget().getClass(),
				RequestMapping.class);
		String method = requestMapping.method().length > 0 ? requestMapping.method()[0].toString() : "GET";
		StringBuilder sb = new StringBuilder("");
		if (requestMappingWithClass != null) {
			String[] pathWithClass = requestMappingWithClass.path();
			if (pathWithClass.length > 0) {
				sb.append(pathWithClass[0]);
			}
		}
		String[] path = requestMapping.path();
		sb.append(path.length > 0 ? path[0] : "");
		String uri = sb.toString();
		return Tracing.executeCheckedCallable(
				ReflectionUtils.stringify(((MethodSignature) pjp.getSignature()).getMethod()), pjp::proceed,
				Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER, Tags.COMPONENT.getKey(), "rest",
				Tags.HTTP_METHOD.getKey(), method, Tags.HTTP_URL.getKey(), uri);
	}

	private boolean isDebug() {
		if (Application.current().map(Application::isDevelopment).orElse(false)) {
			return true;
		}
		Span activeSpan = GlobalTracer.get().activeSpan();
		SpanContext ctx = activeSpan != null ? activeSpan.context() : null;
		return (ctx instanceof JaegerSpanContext jsc) && jsc.isDebug();
	}

	private static String getCallSite(Method method, Object target) {
		// is Throwable faster than Thread.currentThread() ?
		StackTraceElement[] elements = new Throwable().getStackTrace();
		String targetClassName = target.getClass().getName();
		boolean found = false;
		for (StackTraceElement element : elements) {
			String className = element.getClassName();
			if ((element.getMethodName().equals(method.getName()) && className.equals(targetClassName))
					|| className.startsWith(targetClassName + "$$")) { // $$EnhancerBySpringCGLIB$$
				found = true;
				continue;
			}
			if (found) {
				return element.toString();
			}
		}
		return null;
	}

}
