package com.example.demo.core.tracing;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.core.Application;

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
					bean = new TracingTransactionManager((PlatformTransactionManager) bean);
					log.info("Wrapped PlatformTransactionManager [{}] with {}", beanName, bean.getClass().getName());
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
