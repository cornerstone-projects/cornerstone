package io.cornerstone.fs;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestPropertySource(properties = { "file-storage.s3.bucket=test", "file-storage.s3.access-key=minioadmin",
		"file-storage.s3.secret-key=minioadmin", "file-storage.s3.hierarchical-directory=true" })
@Testcontainers
public class MinioFileStorageTests extends FileStorageTestBase {

	@Container
	static GenericContainer<?> container = new GenericContainer<>("minio/minio").withCommand("server /data")
			.withExposedPorts(9000);

	@DynamicPropertySource
	static void registerDynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("file-storage.s3.endpoint",
				() -> "http://" + container.getHost() + ":" + container.getFirstMappedPort());
	}

}
