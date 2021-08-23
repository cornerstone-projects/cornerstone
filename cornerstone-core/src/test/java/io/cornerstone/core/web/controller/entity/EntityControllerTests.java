package io.cornerstone.core.web.controller.entity;

import static io.cornerstone.core.web.controller.entity.TestEntityController.PATH_DETAIL;
import static io.cornerstone.core.web.controller.entity.TestEntityController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.cornerstone.test.ControllerTestBase;

@TestPropertySource(properties = "TestEntityController.enabled=true")
@ContextConfiguration(classes = EntityControllerTests.Config.class)
class EntityControllerTests extends ControllerTestBase {

	@Test
	void crud() {
		TestRestTemplate restTemplate = userRestTemplate();
		TestEntity c = new TestEntity();
		c.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c.setName("test");
		c.setDisabled(Boolean.TRUE);

		// create
		ResponseEntity<TestEntity> response = restTemplate.postForEntity(PATH_LIST, c, TestEntity.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		TestEntity testEntity = response.getBody();
		assertThat(testEntity).isNotNull();
		assertThat(testEntity.getId()).isNotNull();
		assertThat(testEntity.getIdNo()).isEqualTo(c.getIdNo());
		assertThat(testEntity.getName()).isEqualTo(c.getName());
		assertThat(testEntity.getDisabled()).isEqualTo(c.getDisabled());
		Long id = testEntity.getId();

		// read
		response = restTemplate.getForEntity(PATH_DETAIL, TestEntity.class, id);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(testEntity);

		// update partial
		TestEntity c2 = new TestEntity();
		c2.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c2.setName("new name");
		c2.setDisabled(Boolean.TRUE);
		TestEntity c3 = restTemplate.patchForObject(PATH_DETAIL, c2, TestEntity.class, id);
		assertThat(c3.getName()).isEqualTo(c2.getName());
		assertThat(c3.getDisabled()).isEqualTo(c2.getDisabled());
		assertThat(c3.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not updatable
		// update full
		c3.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c3.setName("name");
		c3.setDisabled(Boolean.FALSE);
		restTemplate.put(PATH_DETAIL, c3, id);
		TestEntity c4 = restTemplate.getForEntity(PATH_DETAIL, TestEntity.class, id).getBody();
		assertThat(c4).isNotNull();
		assertThat(c4.getDisabled()).isEqualTo(c3.getDisabled());
		assertThat(c4.getName()).isEqualTo(c3.getName());
		assertThat(c4.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not updatable

		// delete
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, id).build(), void.class)
				.getStatusCode()).isSameAs(BAD_REQUEST);
		c4.setDisabled(Boolean.TRUE);
		restTemplate.put(PATH_DETAIL, c4, id);
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, id).build(), void.class)
				.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, TestEntity.class, id).getStatusCode()).isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = userRestTemplate();
		int size = 5;
		List<TestEntity> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			TestEntity c = new TestEntity();
			c.setIdNo(CitizenIdentificationNumberValidator.randomValue());
			c.setName("test" + i);
			c.setDisabled(Boolean.TRUE);
			list.add(restTemplate.postForObject(PATH_LIST, c, TestEntity.class));
		}

		ResponseEntity<ResultPage<TestEntity>> response = restTemplate.exchange(
				RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		ResultPage<TestEntity> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(size);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(size);
		assertThat(page.getResult().get(0).getCreatedDate()).isNull(); // View.List view
		response = restTemplate.exchange(
				RequestEntity.method(GET, URI.create(PATH_LIST + "?page=2&size=1&sort=id,desc")).build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(2);
		assertThat(page.getSize()).isEqualTo(1);

		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?query=test0")).build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);

		ResponseEntity<ResultPage<TestEntity>> response2 = restTemplate.exchange(
				RequestEntity.method(GET, URI.create(PATH_LIST + "?name=test0")).build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				});
		assertThat(response2.getBody()).isEqualTo(response.getBody());

		for (TestEntity c : list)
			restTemplate.delete(PATH_DETAIL, c.getId());
	}

	@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
	@EntityScan(basePackageClasses = TestEntity.class)
	static class Config {

	}
}
