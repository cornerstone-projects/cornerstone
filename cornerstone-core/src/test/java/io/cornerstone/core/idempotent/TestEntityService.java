package io.cornerstone.core.idempotent;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.OptimisticLockingFailureException;

@RequiredArgsConstructor
public class TestEntityService {

	public static final String SEQ_NO_FOR_LOCKING_FAILURE = "000001";

	final TestEntityRepository repository;

	private final AtomicInteger counter = new AtomicInteger();

	public TestEntity save(Request request) {
		if (SEQ_NO_FOR_LOCKING_FAILURE.equals(request.getSeqNo())) {
			int times = this.counter.incrementAndGet();
			if (times % 3 != 0) {
				throw new OptimisticLockingFailureException("Simulated Exception");
			}
		}
		TestEntity entity = new TestEntity();
		entity.setSeqNo(request.getSeqNo());
		return this.repository.save(entity);

	}

}
