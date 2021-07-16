package com.example.demo.core.hibernate.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;

import com.example.demo.SpringApplicationTest;

@SpringApplicationTest(webEnvironment = WebEnvironment.NONE)
public class AuditableTests {

	@Autowired
	AuditableEntityRepository repository;

	@Test
	@WithMockUser(username = "admin")
	public void test() {
		AuditableEntity entity = repository.save(new AuditableEntity());
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
