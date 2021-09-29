package io.cornerstone.core.tracing;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import io.cornerstone.core.Application;
import io.jaegertracing.internal.Constants;
import io.opentracing.Tracer;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import io.opentracing.contrib.jdbc.TracingDataSource;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import lombok.extern.slf4j.Slf4j;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class TracingConfiguration {

	@Bean
	@TracingEnabled
	TracerBuilderCustomizer tracerBuilderCustomizer(Application application) {
		return builder -> {
			builder.withTag("java.version", System.getProperty("java.version"))
					.withTag("server.info", application.getServerInfo())
					.withTag("server.port", application.getServerPort())
					.withTag(Constants.TRACER_HOSTNAME_TAG_KEY, application.getHostName())
					.withTag(Constants.TRACER_IP_TAG_KEY, application.getHostAddress());
		};
	}

	@Bean
	@TracingEnabled
	TracingAspect tracingAspect() {
		return new TracingAspect();
	}

	@Bean
	@TracingEnabled
	static BeanPostProcessor tracingPostProcessor() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof DataSource) {
					bean = new TracingDataSource(GlobalTracer.get(), (DataSource) bean, null, true, null);
					log.info("Wrapped DataSource [{}] with {}", beanName, bean.getClass().getName());
				} else if (bean instanceof PlatformTransactionManager) {
					ProxyFactory pf = new ProxyFactory(bean);
					pf.addAdvice(new MethodInterceptor() {
						@Override
						public Object invoke(MethodInvocation invocation) throws Throwable {
							Method m = invocation.getMethod();
							if (m.getDeclaringClass() == PlatformTransactionManager.class) {
								return Tracing.executeCheckedCallable("transactionManager." + m.getName(),
										() -> invocation.proceed(), "component", "tx");
							}
							return invocation.proceed();
						}
					});
					bean = pf.getProxy();
					log.info("Proxied PlatformTransactionManager [{}] with tracing supports", beanName);
				}
				return bean;
			}
		};
	}

	@ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "false", matchIfMissing = false)
	@Bean
	public Tracer tracer() {
		Tracing.disable();
		return NoopTracerFactory.create();
	}

}
