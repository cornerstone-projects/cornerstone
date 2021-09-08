package io.cornerstone.core.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.Getter;

@ExtendWith(SpringExtension.class)
public abstract class SequenceTestBase {

	@Getter
	private int threads = 5;

	@Getter
	private int loop = 1000;

	private static ExecutorService executorService;

	@Autowired
	private Sequence sample1Sequence;

	@Autowired
	private Sequence sample2Sequence;

	@BeforeEach
	public void setup() {
		executorService = Executors.newFixedThreadPool(getThreads());
	}

	@AfterEach
	public void destroy() {
		executorService.shutdown();
	}

	@Test
	public void testSimple() throws InterruptedException {
		test(false);
	}

	@Test
	public void testCyclic() throws InterruptedException {
		test(true);
	}

	private void test(boolean cyclic) throws InterruptedException {
		final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>(getThreads() * getLoop() * 2);
		final CountDownLatch cdl = new CountDownLatch(getThreads());
		final AtomicInteger count = new AtomicInteger();
		final Sequence seq = cyclic ? sample2Sequence : sample1Sequence;
		long time = System.currentTimeMillis();
		for (int i = 0; i < getThreads(); i++) {
			executorService.execute(() -> {

				for (int j = 0; j < getLoop(); j++) {
					try {
						String id = seq.nextStringValue();
						Long time2 = System.currentTimeMillis();
						Long old = map.putIfAbsent(id, time2);
						if (old == null)
							count.incrementAndGet();
					} catch (Throwable e) {
						e.printStackTrace();
					}

				}
				cdl.countDown();
			});
		}
		cdl.await();
		time = System.currentTimeMillis() - time;
		System.out.println("completed " + count.get() + " requests with concurrency(" + getThreads() + ") in " + time
				+ "ms (tps = " + (int) (((double) count.get() / time) * 1000) + ") using "
				+ seq.getClass().getSimpleName());
		assertThat(map.size()).isEqualTo(getLoop() * getThreads());
	}

}
