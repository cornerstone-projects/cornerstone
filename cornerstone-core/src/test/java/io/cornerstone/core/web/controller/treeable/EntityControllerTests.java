package io.cornerstone.core.web.controller.treeable;

import java.util.List;

import io.cornerstone.core.domain.ResultPage;
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

	private static final String PATH_CHILDREN = PATH_DETAIL + "/children";

	@Test
	void crud() throws Exception {

		// create
		TestEntity parent = new TestEntity("parent");
		TestEntity testEntity = this.restTemplate.postForObject(PATH_LIST, parent, TestEntity.class);
		assertThat(testEntity).isNotNull();
		assertThat(testEntity.getId()).isNotNull();
		assertThat(testEntity.getName()).isEqualTo(parent.getName());
		assertThat(testEntity.getLevel()).isEqualTo(1);

		assertThatExceptionOfType(BadRequest.class).isThrownBy(
				() -> this.restTemplate.postForObject(PATH_LIST, new TestEntity(parent.getName()), TestEntity.class));
		// name already exists

		TestEntity child = new TestEntity("child");
		child.setParent(testEntity);
		child = this.restTemplate.postForObject(PATH_LIST, child, TestEntity.class);
		assertThat(child).isNotNull();
		assertThat(child.getId()).isNotNull();
		assertThat(child.getName()).isEqualTo("child");
		assertThat(child.getLevel()).isEqualTo(2);
		assertThat(child.getParent()).isNotNull();
		assertThat(child.getParent().getId()).isEqualTo(testEntity.getId());
		TestEntity te = new TestEntity(child.getParent(), child.getName(), 0);
		assertThatExceptionOfType(BadRequest.class)
				.isThrownBy(() -> this.restTemplate.postForObject(PATH_LIST, te, TestEntity.class));

		TestEntity child2 = new TestEntity("child2");
		child2.setParent(child.getParent());
		child2 = this.restTemplate.postForObject(PATH_LIST, child2, TestEntity.class);
		assertThat(child2).isNotNull();
		assertThat(child2.getId()).isNotNull();

		// read
		assertThat(this.restTemplate.getForObject(PATH_DETAIL, TestEntity.class, testEntity.getId()))
				.isEqualTo(testEntity);

		// update partial
		TestEntity temp = new TestEntity("new name");
		child = this.restTemplate.patchForObject(PATH_DETAIL, temp, TestEntity.class, child.getId());
		assertThat(child).isNotNull();
		assertThat(child.getName()).isEqualTo(temp.getName());
		assertThat(child.getParent()).isNotNull(); // parent not updated
		assertThat(child.getLevel()).isEqualTo(2);
		TestEntity te2 = new TestEntity(child.getParent(), child.getName(), 0);
		Long child2Id = child2.getId();
		assertThatExceptionOfType(BadRequest.class)
				.isThrownBy(() -> this.restTemplate.patchForObject(PATH_DETAIL, te2, TestEntity.class, child2Id));
		// name already exists

		// update full
		temp.setName("name");
		temp.setParent(null);
		this.restTemplate.put(PATH_DETAIL, temp, child.getId());
		child = this.restTemplate.getForObject(PATH_DETAIL, TestEntity.class, child.getId());
		assertThat(child).isNotNull();
		assertThat(child.getName()).isEqualTo("name");
		assertThat(child.getParent()).isNull();
		assertThat(child.getLevel()).isEqualTo(1);

		// delete
		this.restTemplate.delete(PATH_DETAIL, child2.getId());
		this.restTemplate.delete(PATH_DETAIL, child.getId());
		this.restTemplate.delete(PATH_DETAIL, testEntity.getId());

		assertThatExceptionOfType(NotFound.class)
				.isThrownBy(() -> this.restTemplate.getForObject(PATH_DETAIL, TestEntity.class, testEntity.getId()));

	}

	@Test
	void list() throws Exception {
		TestEntity parent1 = this.restTemplate.postForObject(PATH_LIST, new TestEntity("parent1", 1), TestEntity.class);
		TestEntity parent2 = this.restTemplate.postForObject(PATH_LIST, new TestEntity("parent2", 2), TestEntity.class);
		TestEntity child1 = this.restTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child1", 1),
				TestEntity.class);
		TestEntity child2 = this.restTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child2", 1),
				TestEntity.class);

		ResultPage<TestEntity> page = this.restTemplate.exchange(RequestEntity.method(GET, PATH_LIST).build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();

		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(4);

		page = this.restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?query=child").build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		page = this.restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?name=child").build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		page = this.restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?level=2").build(),
				new ParameterizedTypeReference<ResultPage<TestEntity>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		this.restTemplate.delete(PATH_DETAIL, child1.getId());
		this.restTemplate.delete(PATH_DETAIL, child2.getId());
		this.restTemplate.delete(PATH_DETAIL, parent1.getId());
		this.restTemplate.delete(PATH_DETAIL, parent2.getId());
	}

	@Test
	void children() throws Exception {
		TestEntity parent1 = this.restTemplate.postForObject(PATH_LIST, new TestEntity("parent1", 1), TestEntity.class);
		TestEntity parent2 = this.restTemplate.postForObject(PATH_LIST, new TestEntity("parent2", 2), TestEntity.class);
		TestEntity child1 = this.restTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child1", 1),
				TestEntity.class);
		TestEntity child2 = this.restTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child2", 1),
				TestEntity.class);

		List<TestEntity> children = this.restTemplate.exchange(RequestEntity.method(GET, PATH_CHILDREN, 0).build(),
				new ParameterizedTypeReference<List<TestEntity>>() {
				}).getBody();

		assertThat(children).isNotNull();
		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(parent2.getName());

		children = this.restTemplate.exchange(RequestEntity.method(GET, PATH_CHILDREN, parent1.getId()).build(),
				new ParameterizedTypeReference<List<TestEntity>>() {
				}).getBody();

		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(child2.getName());

		children = this.restTemplate.exchange(RequestEntity.method(GET, PATH_CHILDREN + "?query=parent", 0).build(),
				new ParameterizedTypeReference<List<TestEntity>>() {
				}).getBody();

		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(parent2.getName());

		children = this.restTemplate
				.exchange(RequestEntity.method(GET, PATH_CHILDREN + "?query=" + parent2.getName(), 0).build(),
						new ParameterizedTypeReference<List<TestEntity>>() {
						})
				.getBody();

		assertThat(children).hasSize(1);
		assertThat(children).element(0).extracting("name").isEqualTo(parent2.getName());

		children = this.restTemplate
				.exchange(RequestEntity.method(GET, PATH_CHILDREN + "?query=child", parent1.getId()).build(),
						new ParameterizedTypeReference<List<TestEntity>>() {
						})
				.getBody();
		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(child2.getName());

		children = this.restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN + "?query=" + child2.getName(), parent1.getId()).build(),
				new ParameterizedTypeReference<List<TestEntity>>() {
				}).getBody();
		assertThat(children).hasSize(1);
		assertThat(children).element(0).extracting("name").isEqualTo(child2.getName());

		this.restTemplate.delete(PATH_DETAIL, child1.getId());
		this.restTemplate.delete(PATH_DETAIL, child2.getId());
		this.restTemplate.delete(PATH_DETAIL, parent1.getId());
		this.restTemplate.delete(PATH_DETAIL, parent2.getId());
	}

}
