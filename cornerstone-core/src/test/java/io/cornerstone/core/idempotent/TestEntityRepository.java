package io.cornerstone.core.idempotent;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

	@Transactional(readOnly = true)
	TestEntity findBySeqNo(String seqNo);

}
