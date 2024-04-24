package io.cornerstone.core.idempotent;

import io.cornerstone.core.annotation.Idempotent;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Recover;

class IdempotentTestEntityService extends TestEntityService {

	IdempotentTestEntityService(TestEntityRepository repository) {
		super(repository);
	}

	@Override
	@Idempotent(recover = "tryFind")
	TestEntity save(Request request) {
		return super.save(request);
	}

	@Recover
	TestEntity tryFind(DataIntegrityViolationException ex, Request request) {
		return this.repository.findBySeqNo(request.getSeqNo());
	}

}
