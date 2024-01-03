package io.cornerstone.core.persistence.audit;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class AuditableTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	@WithMockUser(username = "admin")
	void test() {
		TestEntity entity = this.repository.save(new TestEntity());
		flushAndClear();

		Long id = entity.getId();
		assertThat(id).isNotNull();
		entity = this.repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(entity.getCreatedDate()).isNotNull();
		assertThat(entity.getCreatedBy()).isEqualTo("admin");
		assertThat(entity.getLastModifiedDate()).isNull();
		assertThat(entity.getLastModifiedBy()).isNull();

		entity.setName("test");
		entity = this.repository.save(entity);
		flushAndClear();

		assertThat(entity.getCreatedDate()).isNotNull();
		assertThat(entity.getCreatedBy()).isEqualTo("admin");
		assertThat(entity.getLastModifiedDate()).isNotNull();
		assertThat(entity.getLastModifiedBy()).isEqualTo("admin");
	}

}
