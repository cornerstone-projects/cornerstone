package io.cornerstone.core.idempotent;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ContextConfiguration
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class NotIdempotentTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository testEntityRepository;

	@Autowired
	TestEntityService testEntityService;

	@Test
	void withLockingFailure() {
		Request request = new Request();
		request.setSeqNo(TestEntityService.SEQ_NO_FOR_LOCKING_FAILURE);

		TestEntity testEntity = new TestEntity();
		testEntity.setSeqNo(request.getSeqNo());
		TestEntity entity = this.testEntityRepository.save(testEntity);

		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());

		assertThatExceptionOfType(OptimisticLockingFailureException.class)
			.isThrownBy(() -> this.testEntityService.save(request));

		assertThatExceptionOfType(OptimisticLockingFailureException.class)
			.isThrownBy(() -> this.testEntityService.save(request));

		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> this.testEntityService.save(request));
	}

	@Test
	void withoutLockingFailure() {
		Request request = new Request();
		request.setSeqNo("123456");
		TestEntity entity = this.testEntityService.save(request);
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());

		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> this.testEntityService.save(request));
	}

	@Configuration
	static class Config {

		@Bean
		TestEntityService testEntityService(TestEntityRepository repository) {
			return new TestEntityService(repository);
		}

	}

}
