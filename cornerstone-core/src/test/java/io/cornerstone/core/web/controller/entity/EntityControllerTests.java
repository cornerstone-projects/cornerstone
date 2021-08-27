package io.cornerstone.core.web.controller.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.cornerstone.test.WebMvcWithDataJpaTestBase;

@ComponentScan // scan @RestController in this package
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class EntityControllerTests extends WebMvcWithDataJpaTestBase {

	private static final String PATH_LIST = "/testEntities";

	private static final String PATH_DETAIL = "/testEntity/{id}";

	@Test
	void crud() throws Exception {
		TestEntity c = new TestEntity();
		c.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c.setName("test");
		c.setDisabled(Boolean.TRUE);

		// create
		TestEntity testEntity = restTemplate.postForObject(PATH_LIST, c, TestEntity.class);
		assertThat(testEntity).isNotNull();
		assertThat(testEntity.getId()).isNotNull();
		assertThat(testEntity.getIdNo()).isEqualTo(c.getIdNo());
		assertThat(testEntity.getName()).isEqualTo(c.getName());
		assertThat(testEntity.getDisabled()).isEqualTo(c.getDisabled());
		Long id = testEntity.getId();

		// read
		assertThat(restTemplate.getForObject(PATH_DETAIL, TestEntity.class, id)).isEqualTo(testEntity);

		// update partial
		TestEntity c2 = new TestEntity();
		c2.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c2.setName("new name");
		c2.setDisabled(Boolean.TRUE);
		TestEntity c3 = restTemplate.patchForObject(PATH_DETAIL, c2, TestEntity.class, id);
		assertThat(c3).isNotNull();
		assertThat(c3.getName()).isEqualTo(c2.getName());
		assertThat(c3.getDisabled()).isEqualTo(c2.getDisabled());
		assertThat(c3.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not updatable
		// update full
		c3.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c3.setName("name");
		c3.setDisabled(Boolean.FALSE);
		restTemplate.put(PATH_DETAIL, c3, id);
		TestEntity c4 = restTemplate.getForObject(PATH_DETAIL, TestEntity.class, id);
		assertThat(c4).isNotNull();
		assertThat(c4.getDisabled()).isEqualTo(c3.getDisabled());
		assertThat(c4.getName()).isEqualTo(c3.getName());
		assertThat(c4.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not updatable

		// delete
		assertThatThrownBy(() -> restTemplate.delete(PATH_DETAIL, id)).isInstanceOf(BadRequest.class);

		c4.setDisabled(Boolean.TRUE);
		restTemplate.put(PATH_DETAIL, c4, id);
		restTemplate.delete(PATH_DETAIL, id);
		assertThatThrownBy(() -> restTemplate.getForObject(PATH_DETAIL, TestEntity.class, id))
				.isInstanceOf(NotFound.class);
	}

	@Test
	void list() throws Exception {
		int size = 5;
		List<TestEntity> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			TestEntity c = new TestEntity();
			c.setIdNo(CitizenIdentificationNumberValidator.randomValue());
			c.setName("test" + i);
			c.setDisabled(Boolean.TRUE);
			list.add(restTemplate.postForObject(PATH_LIST, c, TestEntity.class));
		}

		ResultPage<TestEntity> page = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST).build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(size);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(size);
		assertThat(page.getResult().get(0).getCreatedDate()).isNull(); // View.List view

		page = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?page=2&size=1&sort=id,desc").build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(2);
		assertThat(page.getSize()).isEqualTo(1);

		page = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?query=test0").build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);

		ResultPage<TestEntity> page2 = restTemplate
				.exchange(RequestEntity.method(GET, PATH_LIST + "?name=test0").build(),
						new ParameterizedTypeReference<ResultPage<TestEntity>>() {
						})
				.getBody();
		assertThat(page2).isEqualTo(page);

		for (TestEntity c : list)
			restTemplate.delete(PATH_DETAIL, c.getId());
	}

}
