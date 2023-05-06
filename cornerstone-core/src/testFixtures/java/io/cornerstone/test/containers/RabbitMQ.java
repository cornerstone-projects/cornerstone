package io.cornerstone.test.containers;

import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;

@ImportAutoConfiguration({ RabbitAutoConfiguration.class, ServiceConnectionAutoConfiguration.class })
@ImportTestcontainers
public class RabbitMQ {

	@Container
	@ServiceConnection
	static RabbitMQContainer container = new RabbitMQContainer("rabbitmq");

}
