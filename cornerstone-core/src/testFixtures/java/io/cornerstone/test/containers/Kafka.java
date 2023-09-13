package io.cornerstone.test.containers;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;

@ImportAutoConfiguration({ KafkaAutoConfiguration.class, ServiceConnectionAutoConfiguration.class })
@ImportTestcontainers(Kafka.class)
public class Kafka {

	@Container
	@ServiceConnection
	static KafkaContainer container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))
		.withEmbeddedZookeeper();

}
