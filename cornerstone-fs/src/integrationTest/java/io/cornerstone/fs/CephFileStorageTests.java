package io.cornerstone.fs;

import io.cornerstone.core.util.CodecUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "file-storage.s3.bucket=test" })
@Testcontainers
class CephFileStorageTests extends FileStorageTestBase {

	private static final String ACCESS_KEY = CodecUtils.nextId(32);

	private static final String SECRET_KEY = CodecUtils.nextId(32);

	@Container
	static GenericContainer<?> container = new GenericContainer<>("quay.io/ceph/daemon")
			.withEnv("CEPH_DEMO_ACCESS_KEY", ACCESS_KEY).withEnv("CEPH_DEMO_SECRET_KEY", SECRET_KEY)
			.withEnv("RGW_NAME", "localhost").withEnv("CEPH_DEMO_UID", "admin").withEnv("NETWORK_AUTO_DETECT", "1")
			.withExposedPorts(8080).withCommand("demo");

	@DynamicPropertySource
	static void registerDynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("file-storage.s3.endpoint",
				() -> "http://" + container.getHost() + ":" + container.getFirstMappedPort());
		registry.add("file-storage.s3.access-key", () -> ACCESS_KEY);
		registry.add("file-storage.s3.secret-key", () -> SECRET_KEY);
	}

}
