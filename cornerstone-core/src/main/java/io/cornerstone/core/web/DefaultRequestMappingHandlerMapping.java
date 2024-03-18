package io.cornerstone.core.web;

import io.cornerstone.core.util.AopUtils;

import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class DefaultRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	private static final ThreadLocal<Object> handlerHolder = new ThreadLocal<>();

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		super.setEmbeddedValueResolver(new StringValueResolver() {
			@Override
			public String resolveStringValue(String strVal) {
				Object handler = handlerHolder.get();
				if (handler != null) {
					strVal = String.valueOf(PARSER.parseExpression(strVal, ParserContext.TEMPLATE_EXPRESSION)
						.getValue(new StandardEvaluationContext(handler)));
				}
				strVal = resolver.resolveStringValue(strVal);
				return strVal;
			}
		});
	}

	@Override
	protected void detectHandlerMethods(Object handler) {
		Object handlerObject = (handler instanceof String s ? obtainApplicationContext().getBean(s) : handler);
		handlerObject = AopUtils.getUltimateTargetObject(handlerObject);
		handlerHolder.set(handlerObject);
		super.detectHandlerMethods(handler);
		handlerHolder.remove();
	}

}
