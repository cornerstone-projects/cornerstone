package io.cornerstone.core.web.controller.entity;

import java.util.ArrayList;
import java.util.List;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.cornerstone.test.WebMvcWithDataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpMethod.GET;

@ComponentScan // scan @RestController in this package
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class EntityControllerTests extends WebMvcWithDataJpaTestBase {

	private static final String PATH_LIST = "/testEntities";

	private static final String PATH_DETAIL = "/testEntity/{id}";

	@Test
	void crud() {
		TestEntity entity = new TestEntity();
		entity.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		entity.setName("test");
		entity.setDisabled(Boolean.TRUE);

		// create
		TestEntity testEntity = this.restTemplate.postForObject(PATH_LIST, entity, TestEntity.class);
		assertThat(testEntity).isNotNull();
		assertThat(testEntity.getId()).isNotNull();
		assertThat(testEntity.getIdNo()).isEqualTo(entity.getIdNo());
		assertThat(testEntity.getName()).isEqualTo(entity.getName());
		assertThat(testEntity.getDisabled()).isEqualTo(entity.getDisabled());
		Long id = testEntity.getId();

		// read
		assertThat(this.restTemplate.getForObject(PATH_DETAIL, TestEntity.class, id)).isEqualTo(testEntity);

		// update partial
		TestEntity entity2 = new TestEntity();
		entity2.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		entity2.setName("new name");
		entity2.setDisabled(Boolean.TRUE);
		TestEntity entity3 = this.restTemplate.patchForObject(PATH_DETAIL, entity2, TestEntity.class, id);
		assertThat(entity3).isNotNull();
		assertThat(entity3.getName()).isEqualTo(entity2.getName());
		assertThat(entity3.getDisabled()).isEqualTo(entity2.getDisabled());
		assertThat(entity3.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not
																		// updatable
		// update full
		entity3.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		entity3.setName("name");
		entity3.setDisabled(Boolean.FALSE);
		this.restTemplate.put(PATH_DETAIL, entity3, id);
		TestEntity c4 = this.restTemplate.getForObject(PATH_DETAIL, TestEntity.class, id);
		assertThat(c4).isNotNull();
		assertThat(c4.getDisabled()).isEqualTo(entity3.getDisabled());
		assertThat(c4.getName()).isEqualTo(entity3.getName());
		assertThat(c4.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not updatable

		// delete
		assertThatExceptionOfType(BadRequest.class).isThrownBy(() -> this.restTemplate.delete(PATH_DETAIL, id));

		c4.setDisabled(Boolean.TRUE);
		this.restTemplate.put(PATH_DETAIL, c4, id);
		this.restTemplate.delete(PATH_DETAIL, id);
		assertThatExceptionOfType(NotFound.class)
				.isThrownBy(() -> this.restTemplate.getForObject(PATH_DETAIL, TestEntity.class, id));
	}

	@Test
	void list() {
		int size = 5;
		List<TestEntity> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setIdNo(CitizenIdentificationNumberValidator.randomValue());
			entity.setName("test" + i);
			entity.setDisabled(Boolean.TRUE);
			list.add(this.restTemplate.postForObject(PATH_LIST, entity, TestEntity.class));
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
		assertThat(page.getResult().get(0).getCreatedDate()).isNull(); // View.List view

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

		for (TestEntity entity : list) {
			this.restTemplate.delete(PATH_DETAIL, entity.getId());
		}
	}

}
