package io.cornerstone.core.idempotent;

import io.cornerstone.core.annotation.Idempotent;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Recover;

public class IdempotentTestEntityService extends TestEntityService {

	public IdempotentTestEntityService(TestEntityRepository repository) {
		super(repository);
	}

	@Override
	@Idempotent(recover = "tryFind")
	public TestEntity save(Request request) {
		return super.save(request);
	}

	@Recover
	public TestEntity tryFind(DataIntegrityViolationException ex, Request request) {
		return this.repository.findBySeqNo(request.getSeqNo());
	}

}
