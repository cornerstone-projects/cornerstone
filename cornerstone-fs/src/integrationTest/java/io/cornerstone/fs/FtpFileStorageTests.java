package io.cornerstone.fs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestPropertySource(properties = { "file-storage.ftp.data-timeout=30000", "file-storage.ftp.buffer-threshold=8",
		"file-storage.ftp.pool.max-total=5" })
@Testcontainers
class FtpFileStorageTests extends FileStorageTestBase {

	private static final String FTP_USER = "ftp";

	private static final String FTP_PASSWORD = "ftp";

	@SuppressWarnings("rawtypes")
	@Container
	static GenericContainer<?> container = (new GenericContainer("panubo/vsftpd") {
		GenericContainer<?> withFixedExposedPort() {
			for (int port = 4559; port <= 4564; port++)
				addFixedExposedPort(port, port);
			return this;
		}
	}).withFixedExposedPort().withExposedPorts(21).withEnv("FTP_USER", FTP_USER).withEnv("FTP_PASSWORD", FTP_PASSWORD)
			.withEnv("FTP_CHOWN_ROOT", "true");

	@DynamicPropertySource
	static void registerDynamicProperties(DynamicPropertyRegistry registry) {
		StringBuilder sb = new StringBuilder("ftp://");
		sb.append(FTP_USER).append(':').append(FTP_PASSWORD).append('@');
		sb.append(container.getHost()).append(':').append(container.getMappedPort(21));
		registry.add("file-storage.uri", sb::toString);
	}

	@Test
	void testConcurrency() throws Exception {

		int concurrency = 2;
		int loop = 20;
		ExecutorService es = Executors.newFixedThreadPool(concurrency);
		CountDownLatch cdl = new CountDownLatch(concurrency);
		AtomicInteger errors = new AtomicInteger();
		for (int i = 0; i < concurrency; i++) {
			final int j = i;
			es.execute(() -> {
				String path = "/test" + j + ".txt";
				try {
					for (int k = 0; k < loop; k++) {
						String text = "test" + j + "-" + k;
						writeToFile(this.fs, text, path);
						try (BufferedReader br = new BufferedReader(
								new InputStreamReader(this.fs.open(path), StandardCharsets.UTF_8))) {
							if (!text.equals(br.lines().collect(Collectors.joining("\n"))))
								errors.incrementAndGet();
						}
						this.fs.delete(path);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					errors.incrementAndGet();
				} finally {

					cdl.countDown();
				}
			});
		}
		cdl.await();
		es.shutdown();

		assertThat(errors.intValue(), is(0));
	}

}
