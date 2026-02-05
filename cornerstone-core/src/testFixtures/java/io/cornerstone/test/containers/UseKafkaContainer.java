package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@ImportTestcontainers(UseKafkaContainer.Containers.class)
@ImportAutoConfiguration({ ServiceConnectionAutoConfiguration.class, KafkaAutoConfiguration.class })
public @interface UseKafkaContainer {

	class Containers {

		@Container
		@ServiceConnection
		static ConfluentKafkaContainer container = new ConfluentKafkaContainer(
				DockerImageName.parse("confluentinc/cp-kafka"));

	}

}
