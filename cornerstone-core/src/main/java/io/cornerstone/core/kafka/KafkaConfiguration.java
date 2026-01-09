package io.cornerstone.core.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.listener.BatchInterceptor;
import org.springframework.kafka.listener.CompositeBatchInterceptor;

@Configuration(proxyBeanMethods = false)
public class KafkaConfiguration {

	@Bean
	@Primary
	CompositeBatchInterceptor<Object, Object> compositeBatchInterceptor(BatchInterceptor<Object, Object>[] delegates) {
		return new CompositeBatchInterceptor<>(delegates);
	}

	@Bean
	ErrorHandlingBatchInterceptor<Object, Object> errorHandlingBatchInterceptor() {
		return new ErrorHandlingBatchInterceptor<>();
	}

}
