package io.cornerstone.core.web;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class DefaultWebMvcRegistrations implements WebMvcRegistrations {

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return new RequestMappingHandlerMapping() {

			protected final Log logger = LogFactory.getLog(RequestMappingHandlerMapping.class); // logging.group.web

			@Nullable
			private HandlerEmbeddedValueResolver embeddedValueResolver;

			@Override
			public void setEmbeddedValueResolver(StringValueResolver resolver) {
				super.setEmbeddedValueResolver(resolver);
				this.embeddedValueResolver = new HandlerEmbeddedValueResolver(resolver);
			}

			protected String[] resolveEmbeddedValuesInPatterns(String[] patterns, Object handlerObject) {
				if (this.embeddedValueResolver == null) {
					return patterns;
				} else {
					this.embeddedValueResolver.setHandler(handlerObject);
					String[] resolvedPatterns = new String[patterns.length];
					for (int i = 0; i < patterns.length; i++) {
						resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
					}
					return resolvedPatterns;
				}
			}

			@Override
			protected void detectHandlerMethods(Object handler) {
				Class<?> handlerType = (handler instanceof String ? obtainApplicationContext().getType((String) handler)
						: handler.getClass());

				if (handlerType != null) {
					Object handlerObject = (handler instanceof String
							? obtainApplicationContext().getBean((String) handler)
							: handler);
					Class<?> userType = ClassUtils.getUserClass(handlerType);
					Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
							(MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
								try {
									return getMappingForMethod(method, userType, handlerObject);
								} catch (Throwable ex) {
									throw new IllegalStateException(
											"Invalid mapping on handler class [" + userType.getName() + "]: " + method,
											ex);
								}
							});
					if (logger.isTraceEnabled()) {
						logger.trace(formatMappings(userType, methods));
					} else if (mappingsLogger.isDebugEnabled()) {
						mappingsLogger.debug(formatMappings(userType, methods));
					}
					methods.forEach((method, mapping) -> {
						Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
						registerHandlerMethod(handler, invocableMethod, mapping);
					});
				}
			}

			@Nullable
			protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType,
					Object handlerObject) {
				RequestMappingInfo info = createRequestMappingInfo(method, handlerObject);
				if (info != null) {
					RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType, handlerObject);
					if (typeInfo != null) {
						info = typeInfo.combine(info);
					}
					String prefix = doGetPathPrefix(handlerType);
					if (prefix != null) {
						info = RequestMappingInfo.paths(prefix).options(getConfig()).build().combine(info);
					}
				}
				return info;
			}

			@Nullable
			private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element, Object handlerObject) {
				RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element,
						RequestMapping.class);
				RequestCondition<?> condition = (element instanceof Class ? getCustomTypeCondition((Class<?>) element)
						: getCustomMethodCondition((Method) element));
				return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition, handlerObject)
						: null);
			}

			protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping,
					@Nullable RequestCondition<?> customCondition, Object handlerObject) {

				RequestMappingInfo.Builder builder = RequestMappingInfo
						.paths(resolveEmbeddedValuesInPatterns(requestMapping.path(), handlerObject))
						.methods(requestMapping.method()).params(requestMapping.params())
						.headers(requestMapping.headers()).consumes(requestMapping.consumes())
						.produces(requestMapping.produces()).mappingName(requestMapping.name());
				if (customCondition != null) {
					builder.customCondition(customCondition);
				}
				return builder.options(getConfig()).build();
			}

			@Nullable
			private String doGetPathPrefix(Class<?> handlerType) {
				for (Map.Entry<String, Predicate<Class<?>>> entry : this.getPathPrefixes().entrySet()) {
					if (entry.getValue().test(handlerType)) {
						String prefix = entry.getKey();
						if (this.embeddedValueResolver != null) {
							prefix = this.embeddedValueResolver.resolveStringValue(prefix);
						}
						return prefix;
					}
				}
				return null;
			}

			private RequestMappingInfo.BuilderConfiguration getConfig() {
				try {
					Field f = RequestMappingHandlerMapping.class.getDeclaredField("config");
					f.setAccessible(true);
					return (RequestMappingInfo.BuilderConfiguration) f.get(this);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			private String formatMappings(Class<?> userType, Map<Method, RequestMappingInfo> methods) {
				String formattedType = Arrays.stream(ClassUtils.getPackageName(userType).split("\\."))
						.map(p -> p.substring(0, 1))
						.collect(Collectors.joining(".", "", "." + userType.getSimpleName()));
				Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes())
						.map(Class::getSimpleName).collect(Collectors.joining(",", "(", ")"));
				return methods.entrySet().stream().map(e -> {
					Method method = e.getKey();
					return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
				}).collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
			}

		};
	}

	static class HandlerEmbeddedValueResolver implements StringValueResolver {

		private static final SpelExpressionParser PARSER = new SpelExpressionParser();

		private final @Nullable StringValueResolver delegate;

		private @Nullable StandardEvaluationContext context;

		public HandlerEmbeddedValueResolver(@Nullable StringValueResolver delegate) {
			this.delegate = delegate;
		}

		public void setHandler(Object handler) {
			if (handler != null) {
				this.context = new StandardEvaluationContext(handler);
			} else {
				this.context = null;
			}
		}

		@Override
		public String resolveStringValue(String strVal) {
			if (this.context != null) {
				strVal = String.valueOf(
						PARSER.parseExpression(strVal, ParserContext.TEMPLATE_EXPRESSION).getValue(this.context));
			}
			if (this.delegate != null) {
				strVal = this.delegate.resolveStringValue(strVal);
			}
			return strVal;
		}

	}

}
