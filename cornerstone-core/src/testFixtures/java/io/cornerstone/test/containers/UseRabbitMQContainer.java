package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;
import org.springframework.context.annotation.Bean;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@ImportTestcontainers(UseRabbitMQContainer.Containers.class)
@ImportAutoConfiguration({ ServiceConnectionAutoConfiguration.class, RabbitAutoConfiguration.class,
		UseRabbitMQContainer.Config.class })
public @interface UseRabbitMQContainer {

	class Containers {

		@Container
		@ServiceConnection
		static RabbitMQContainer container = new RabbitMQContainer("rabbitmq");

	}

	class Config {

		@Bean
		SimpleMessageConverter converter() {
			SimpleMessageConverter converter = new SimpleMessageConverter();
			converter.setAllowedListPatterns(List.of("*"));
			return converter;
		}

	}

}
