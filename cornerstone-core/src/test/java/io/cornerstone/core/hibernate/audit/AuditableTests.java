package io.cornerstone.core.hibernate.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.test.context.support.WithMockUser;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
public class AuditableTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	@WithMockUser(username = "admin")
	public void test() {
		TestEntity entity = repository.save(new TestEntity());
		entity = repository.findById(entity.getId()).orElseThrow(IllegalStateException::new);
		assertThat(entity.getCreatedDate()).isNotNull();
		assertThat(entity.getCreatedBy()).isEqualTo("admin");
		assertThat(entity.getLastModifiedDate()).isNull();
		assertThat(entity.getLastModifiedBy()).isNull();

		entity.setName("test");
		entity = repository.save(entity);
		assertThat(entity.getCreatedDate()).isNotNull();
		assertThat(entity.getCreatedBy()).isEqualTo("admin");
		assertThat(entity.getLastModifiedDate()).isNotNull();
		assertThat(entity.getLastModifiedBy()).isEqualTo("admin");
	}

}
