package io.cornerstone.core.idempotent;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ContextConfiguration
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class IdempotentTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository testEntityRepository;

	@SpyBean
	IdempotentTestEntityService testEntityService;

	@Test
	void withLockingFailure() {
		Request request = new Request();
		request.setSeqNo(TestEntityService.SEQ_NO_FOR_LOCKING_FAILURE);

		TestEntity entity = this.testEntityService.save(request);
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());
		then(this.testEntityService).should(times(3)).save(request);
		then(this.testEntityService).should(never()).tryFind(any(DataIntegrityViolationException.class), eq(request));

		entity = this.testEntityService.save(request);
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());
		then(this.testEntityService).should(times(6)).save(request);
		then(this.testEntityService).should(times(1)).tryFind(any(DataIntegrityViolationException.class), eq(request));

		entity = this.testEntityService.save(request);
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());
		then(this.testEntityService).should(times(9)).save(request);
		then(this.testEntityService).should(times(2)).tryFind(any(DataIntegrityViolationException.class), eq(request));

	}

	@Test
	void withoutLockingFailure() {
		Request request = new Request();
		request.setSeqNo("123456");
		TestEntity entity = this.testEntityService.save(request);
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());
		then(this.testEntityService).should(times(1)).save(request);
		then(this.testEntityService).should(never()).tryFind(any(DataIntegrityViolationException.class), eq(request));

		entity = this.testEntityService.save(request);
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());
		then(this.testEntityService).should(times(2)).save(request);
		then(this.testEntityService).should(times(1)).tryFind(any(DataIntegrityViolationException.class), eq(request));

		entity = this.testEntityService.save(request);
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getSeqNo()).isEqualTo(request.getSeqNo());
		then(this.testEntityService).should(times(3)).save(request);
		then(this.testEntityService).should(times(2)).tryFind(any(DataIntegrityViolationException.class), eq(request));
	}

	@Configuration
	@EnableRetry(order = Ordered.HIGHEST_PRECEDENCE)
	static class Config {

		@Bean
		TestEntityService testEntityService(TestEntityRepository repository) {
			return new IdempotentTestEntityService(repository);
		}

	}

}
