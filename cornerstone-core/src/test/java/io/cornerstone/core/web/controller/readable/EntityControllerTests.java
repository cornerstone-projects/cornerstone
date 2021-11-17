package io.cornerstone.core.web.controller.readable;

import java.util.ArrayList;
import java.util.List;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.test.WebMvcWithDataJpaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.HttpClientErrorException.MethodNotAllowed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpMethod.GET;

@ComponentScan // scan @RestController in this package
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class EntityControllerTests extends WebMvcWithDataJpaTestBase {

	private static final String PATH_LIST = "/testEntities";

	private static final String PATH_DETAIL = "/testEntity/{id}";

	@Autowired
	private TestEntityRepository repository;

	@AfterEach
	void cleanup() {
		this.repository.deleteAll();
	}

	@Test
	void get() {
		TestEntity entity = new TestEntity();
		entity.setName("test");
		this.repository.save(entity);
		assertThat(this.restTemplate.getForObject(PATH_DETAIL, TestEntity.class, entity.getId())).isEqualTo(entity);
	}

	@Test
	void list() {
		int size = 5;
		List<TestEntity> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setName("test" + i);
			list.add(this.repository.save(entity));
		}

		ResultPage<TestEntity> page = this.restTemplate.exchange(RequestEntity.method(GET, PATH_LIST).build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(size);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(size);

		page = this.restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?page=2&size=1&sort=id,desc").build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(2);
		assertThat(page.getSize()).isEqualTo(1);

		page = this.restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?query=test0").build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);

		ResultPage<TestEntity> page2 = this.restTemplate
				.exchange(RequestEntity.method(GET, PATH_LIST + "?name=test0").build(),
						new ParameterizedTypeReference<ResultPage<TestEntity>>() {
						})
				.getBody();
		assertThat(page2).isEqualTo(page);
	}

	@Test
	void ensureNotWritable() {
		TestEntity entity = new TestEntity();
		entity.setName("test");

		assertThatExceptionOfType(MethodNotAllowed.class)
				.isThrownBy(() -> this.restTemplate.postForEntity(PATH_LIST, entity, TestEntity.class));

		this.repository.save(entity);

		assertThatExceptionOfType(MethodNotAllowed.class)
				.isThrownBy(() -> this.restTemplate.put(PATH_DETAIL, entity, entity.getId()));

		assertThatExceptionOfType(MethodNotAllowed.class).isThrownBy(
				() -> this.restTemplate.patchForObject(PATH_DETAIL, entity, TestEntity.class, entity.getId()));

		assertThatExceptionOfType(MethodNotAllowed.class)
				.isThrownBy(() -> this.restTemplate.delete(PATH_DETAIL, entity.getId()));
	}

}
