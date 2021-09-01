package io.cornerstone.fs;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.clevercloud.testcontainers.ceph.CephContainer;

@TestPropertySource(properties = { "file-storage.s3.bucket=test" })
@Testcontainers
public class CephFileStorageTests extends FileStorageTestBase {

	@Container
	static CephContainer container = new CephContainer();

	@DynamicPropertySource
	static void registerDynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("file-storage.s3.endpoint",
				() -> container.getRGWProtocol() + "://" + container.getRGWHTTPHostAddress());
		registry.add("file-storage.s3.access-key", container::getRGWAccessKey);
		registry.add("file-storage.s3.secret-key", container::getRGWSecretKey);
	}

}
