package io.cornerstone.test.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;

import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
@Import(RabbitAutoConfiguration.class)
public class RabbitMQ extends AbstractContainer {

	@Override
	protected GenericContainer<?> createContainer() {
		return new RabbitMQContainer(getImage());
	}

	@Primary
	@Bean
	public RabbitProperties rabbitProperties(GenericContainer<?> rabbitmqContainer) {
		RabbitMQContainer container = (RabbitMQContainer) rabbitmqContainer;
		RabbitProperties properties = new RabbitProperties();
		PropertyMapper map = PropertyMapper.get();
		map.from(container::getHost).to(properties::setHost);
		map.from(container::getAmqpPort).to(properties::setPort);
		map.from(container::getAdminUsername).to(properties::setUsername);
		map.from(container::getAdminPassword).to(properties::setPassword);
		return properties;
	}

}
