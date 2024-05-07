package io.cornerstone.core.throttle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.cornerstone.core.coordination.LockFailedException;
import io.cornerstone.core.coordination.LockService;
import io.cornerstone.core.coordination.impl.StandaloneLockService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringJUnitConfig
public class MutexAspectTests {

	@Autowired
	private EchoService echoService;

	@Test
	void test() {
		assertThatExceptionOfType(LockFailedException.class).isThrownBy(() -> {
			int concurrency = 2;
			ExecutorService es = Executors.newFixedThreadPool(concurrency);
			Collection<Callable<String>> tasks = new ArrayList<>();
			for (int i = 0; i < concurrency; i++) {
				tasks.add(() -> this.echoService.echo("test"));
			}
			List<Future<String>> results = es.invokeAll(tasks);
			try {
				for (Future<String> f : results) {
					f.get();
				}
			}
			catch (ExecutionException ex) {
				throw ex.getCause();
			}
			finally {
				es.shutdown();
			}
		});
	}

	public static class EchoService {

		@Mutex(key = "${s}")
		public String echo(String s) throws Exception {
			Thread.sleep(100);
			return s;
		}

	}

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class Config {

		@Bean
		LockService lockService() {
			return new StandaloneLockService();
		}

		@Bean
		EchoService echoService() {
			return new EchoService();
		}

		@Bean
		MutexAspect mutexAspect() {
			return new MutexAspect();
		}

	}

}
