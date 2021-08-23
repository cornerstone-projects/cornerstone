package io.cornerstone.core.web.controller.treeable;

import static io.cornerstone.core.web.controller.treeable.TestTreeableEntityController.PATH_CHILDREN;
import static io.cornerstone.core.web.controller.treeable.TestTreeableEntityController.PATH_DETAIL;
import static io.cornerstone.core.web.controller.treeable.TestTreeableEntityController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
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

import io.cornerstone.test.ControllerTestBase;

@TestPropertySource(properties = "TestTreeableEntityController.enabled=true")
@ContextConfiguration(classes = TreeableEntityControllerTests.Config.class)
class TreeableEntityControllerTests extends ControllerTestBase {

	@Test
	void crud() {
		TestRestTemplate restTemplate = userRestTemplate();

		// create
		TestTreeableEntity parent = new TestTreeableEntity("parent");
		ResponseEntity<TestTreeableEntity> response = restTemplate.postForEntity(PATH_LIST, parent,
				TestTreeableEntity.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		TestTreeableEntity testTreeableEntity = response.getBody();
		assertThat(testTreeableEntity).isNotNull();
		assertThat(testTreeableEntity.getId()).isNotNull();
		assertThat(testTreeableEntity.getName()).isEqualTo(parent.getName());
		assertThat(testTreeableEntity.getLevel()).isEqualTo(1);

		assertThat(restTemplate.postForEntity(PATH_LIST, new TestTreeableEntity(testTreeableEntity.getName()),
				TestTreeableEntity.class).getStatusCode()).isSameAs(BAD_REQUEST); // name already exists

		TestTreeableEntity child = new TestTreeableEntity("child");
		child.setParent(testTreeableEntity);
		response = restTemplate.postForEntity(PATH_LIST, child, TestTreeableEntity.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		child = response.getBody();
		assertThat(child).isNotNull();
		assertThat(child.getId()).isNotNull();
		assertThat(child.getName()).isEqualTo("child");
		assertThat(child.getLevel()).isEqualTo(2);
		assertThat(child.getParent()).isNotNull();
		assertThat(child.getParent().getId()).isEqualTo(testTreeableEntity.getId());
		assertThat(restTemplate.postForEntity(PATH_LIST, new TestTreeableEntity(child.getParent(), child.getName(), 0),
				TestTreeableEntity.class).getStatusCode()).isSameAs(BAD_REQUEST);

		TestTreeableEntity child2 = new TestTreeableEntity("child2");
		child2.setParent(child.getParent());
		child2 = restTemplate.postForObject(PATH_LIST, child2, TestTreeableEntity.class);
		assertThat(child2.getId()).isNotNull();

		// read
		response = restTemplate.getForEntity(PATH_DETAIL, TestTreeableEntity.class, testTreeableEntity.getId());
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(testTreeableEntity);

		// update partial
		TestTreeableEntity temp = new TestTreeableEntity("new name");
		child = restTemplate.patchForObject(PATH_DETAIL, temp, TestTreeableEntity.class, child.getId());
		assertThat(child.getName()).isEqualTo(temp.getName());
		assertThat(child.getParent()).isNotNull(); // parent not updated
		assertThat(child.getLevel()).isEqualTo(2);
		assertThat(
				restTemplate.exchange(
						RequestEntity.method(PATCH, PATH_DETAIL, child2.getId())
								.body(new TestTreeableEntity(child.getParent(), child.getName(), 0)),
						TestTreeableEntity.class).getStatusCode()).isSameAs(BAD_REQUEST); // name already exists

		// update full
		temp.setName("name");
		temp.setParent(null);
		restTemplate.put(PATH_DETAIL, temp, child.getId());
		child = restTemplate.getForEntity(PATH_DETAIL, TestTreeableEntity.class, child.getId()).getBody();
		assertThat(child).isNotNull();
		assertThat(child.getName()).isEqualTo("name");
		assertThat(child.getParent()).isNull();
		assertThat(child.getLevel()).isEqualTo(1);

		// delete
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, child2.getId()).build(), void.class)
				.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, child.getId()).build(), void.class)
				.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate
				.exchange(RequestEntity.method(DELETE, PATH_DETAIL, testTreeableEntity.getId()).build(), void.class)
				.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, TestTreeableEntity.class, testTreeableEntity.getId())
				.getStatusCode()).isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = userRestTemplate();
		TestTreeableEntity parent1 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity("parent1", 1),
				TestTreeableEntity.class);
		TestTreeableEntity parent2 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity("parent2", 2),
				TestTreeableEntity.class);
		TestTreeableEntity child1 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity(parent1, "child1", 1),
				TestTreeableEntity.class);
		TestTreeableEntity child2 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity(parent1, "child2", 1),
				TestTreeableEntity.class);

		ResponseEntity<List<TestTreeableEntity>> response = restTemplate.exchange(
				RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).hasSize(4);

		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?query=child")).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(2);

		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?name=child")).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(2);

		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?level=2")).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(2);

		restTemplate.delete(PATH_DETAIL, child1.getId());
		restTemplate.delete(PATH_DETAIL, child2.getId());
		restTemplate.delete(PATH_DETAIL, parent1.getId());
		restTemplate.delete(PATH_DETAIL, parent2.getId());
	}

	@Test
	void children() {
		TestRestTemplate restTemplate = userRestTemplate();
		TestTreeableEntity parent1 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity("parent1", 1),
				TestTreeableEntity.class);
		TestTreeableEntity parent2 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity("parent2", 2),
				TestTreeableEntity.class);
		TestTreeableEntity child1 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity(parent1, "child1", 1),
				TestTreeableEntity.class);
		TestTreeableEntity child2 = restTemplate.postForObject(PATH_LIST, new TestTreeableEntity(parent1, "child2", 1),
				TestTreeableEntity.class);

		ResponseEntity<List<TestTreeableEntity>> response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN, 0).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(parent2.getName());

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_CHILDREN, parent1.getId()).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(child2.getName());

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_CHILDREN + "?query=parent", 0).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(parent2.getName());

		response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN + "?query=" + parent2.getName(), 0).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(parent2.getName());

		response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN + "?query=child", parent1.getId()).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(child2.getName());

		response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN + "?query=" + child2.getName(), parent1.getId()).build(),
				new ParameterizedTypeReference<List<TestTreeableEntity>>() {
				});
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(child2.getName());

		restTemplate.delete(PATH_DETAIL, child1.getId());
		restTemplate.delete(PATH_DETAIL, child2.getId());
		restTemplate.delete(PATH_DETAIL, parent1.getId());
		restTemplate.delete(PATH_DETAIL, parent2.getId());
	}

	@EnableJpaRepositories(basePackageClasses = TestTreeableEntityRepository.class)
	@EntityScan(basePackageClasses = TestTreeableEntity.class)
	static class Config {

	}
}
