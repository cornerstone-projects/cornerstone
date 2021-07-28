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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@Disabled
@TestPropertySource(properties = { "file-storage.uri=ftp://admin:admin@localhost:2121/temp",
		"file-storage.ftp.data-timeout=30000", "file-storage.ftp.buffer-threshold=8",
		"file-storage.ftp.pool.max-total=5" })
public class FtpFileStorageTests extends FileStorageTestBase {

	@Test
	public void testConcurrency() throws Exception {

		int concurrency = 50;
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
						writeToFile(fs, text, path);
						try (BufferedReader br = new BufferedReader(
								new InputStreamReader(fs.open(path), StandardCharsets.UTF_8))) {
							if (!text.equals(br.lines().collect(Collectors.joining("\n"))))
								errors.incrementAndGet();
						}
						fs.delete(path);
					}
				} catch (Exception e) {
					e.printStackTrace();
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
