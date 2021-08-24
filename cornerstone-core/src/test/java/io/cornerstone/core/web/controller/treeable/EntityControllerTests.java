package io.cornerstone.core.web.controller.treeable;

import static io.cornerstone.core.web.controller.treeable.TestEntityController.PATH_CHILDREN;
import static io.cornerstone.core.web.controller.treeable.TestEntityController.PATH_DETAIL;
import static io.cornerstone.core.web.controller.treeable.TestEntityController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.test.WebMvcWithDataJpaTestBase;

@ContextConfiguration(classes = EntityControllerTests.Config.class)
class EntityControllerTests extends WebMvcWithDataJpaTestBase {

	@Test
	void crud() throws Exception {

		// create
		TestEntity parent = new TestEntity("parent");
		TestEntity testEntity = mockMvcRestTemplate.postForObject(PATH_LIST, parent, TestEntity.class);
		assertThat(testEntity).isNotNull();
		assertThat(testEntity.getId()).isNotNull();
		assertThat(testEntity.getName()).isEqualTo(parent.getName());
		assertThat(testEntity.getLevel()).isEqualTo(1);

		mockMvcRestTemplate.postForResult(PATH_LIST, new TestEntity(testEntity.getName()))
				.andExpect(status().isBadRequest());
		// name already exists

		TestEntity child = new TestEntity("child");
		child.setParent(testEntity);
		child = mockMvcRestTemplate.postForObject(PATH_LIST, child, TestEntity.class);
		assertThat(child).isNotNull();
		assertThat(child.getId()).isNotNull();
		assertThat(child.getName()).isEqualTo("child");
		assertThat(child.getLevel()).isEqualTo(2);
		assertThat(child.getParent()).isNotNull();
		assertThat(child.getParent().getId()).isEqualTo(testEntity.getId());
		mockMvcRestTemplate.postForResult(PATH_LIST, new TestEntity(child.getParent(), child.getName(), 0))
				.andExpect(status().isBadRequest());

		TestEntity child2 = new TestEntity("child2");
		child2.setParent(child.getParent());
		child2 = mockMvcRestTemplate.postForObject(PATH_LIST, child2, TestEntity.class);
		assertThat(child2.getId()).isNotNull();

		// read
		assertThat(mockMvcRestTemplate.getForObject(PATH_DETAIL, TestEntity.class, testEntity.getId()))
				.isEqualTo(testEntity);

		// update partial
		TestEntity temp = new TestEntity("new name");
		child = mockMvcRestTemplate.patchForObject(PATH_DETAIL, temp, TestEntity.class, child.getId());
		assertThat(child.getName()).isEqualTo(temp.getName());
		assertThat(child.getParent()).isNotNull(); // parent not updated
		assertThat(child.getLevel()).isEqualTo(2);
		mockMvcRestTemplate
				.patchForResult(PATH_DETAIL, new TestEntity(child.getParent(), child.getName(), 0), child2.getId())
				.andExpect(status().isBadRequest());
		// name already exists

		// update full
		temp.setName("name");
		temp.setParent(null);
		mockMvcRestTemplate.put(PATH_DETAIL, temp, child.getId());
		child = mockMvcRestTemplate.getForObject(PATH_DETAIL, TestEntity.class, child.getId());
		assertThat(child).isNotNull();
		assertThat(child.getName()).isEqualTo("name");
		assertThat(child.getParent()).isNull();
		assertThat(child.getLevel()).isEqualTo(1);

		// delete
		mockMvcRestTemplate.delete(PATH_DETAIL, child2.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, child.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, testEntity.getId());

		mockMvcRestTemplate.getForResult(PATH_DETAIL, testEntity.getId()).andExpect(status().isNotFound());
	}

	@Test
	void list() throws Exception {
		TestEntity parent1 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity("parent1", 1),
				TestEntity.class);
		TestEntity parent2 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity("parent2", 2),
				TestEntity.class);
		TestEntity child1 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child1", 1),
				TestEntity.class);
		TestEntity child2 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child2", 1),
				TestEntity.class);

		ResultPage<TestEntity> page = mockMvcRestTemplate.getForObject(PATH_LIST, new TypeReference<>() {
		});
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(4);

		page = mockMvcRestTemplate.getForObject(PATH_LIST + "?query=child", new TypeReference<>() {
		});
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		page = mockMvcRestTemplate.getForObject(PATH_LIST + "?name=child", new TypeReference<>() {
		});
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		page = mockMvcRestTemplate.getForObject(PATH_LIST + "?level=2", new TypeReference<>() {
		});
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		mockMvcRestTemplate.delete(PATH_DETAIL, child1.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, child2.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, parent1.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, parent2.getId());
	}

	@Test
	void children() throws Exception {
		TestEntity parent1 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity("parent1", 1),
				TestEntity.class);
		TestEntity parent2 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity("parent2", 2),
				TestEntity.class);
		TestEntity child1 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child1", 1),
				TestEntity.class);
		TestEntity child2 = mockMvcRestTemplate.postForObject(PATH_LIST, new TestEntity(parent1, "child2", 1),
				TestEntity.class);

		List<TestEntity> children = mockMvcRestTemplate.getForObject(PATH_CHILDREN, new TypeReference<>() {
		}, 0);
		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(parent2.getName());

		children = mockMvcRestTemplate.getForObject(PATH_CHILDREN, new TypeReference<>() {
		}, parent1.getId());
		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(child2.getName());

		children = mockMvcRestTemplate.getForObject(PATH_CHILDREN + "?query=parent", new TypeReference<>() {
		}, 0);
		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(parent2.getName());

		children = mockMvcRestTemplate.getForObject(PATH_CHILDREN + "?query=" + parent2.getName(),
				new TypeReference<>() {
				}, 0);

		assertThat(children).hasSize(1);
		assertThat(children).element(0).extracting("name").isEqualTo(parent2.getName());

		children = mockMvcRestTemplate.getForObject(PATH_CHILDREN + "?query=child", new TypeReference<>() {
		}, parent1.getId());
		assertThat(children).hasSize(2);
		assertThat(children).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(children).element(1).extracting("name").isEqualTo(child2.getName());

		children = mockMvcRestTemplate.getForObject(PATH_CHILDREN + "?query=" + child2.getName(),
				new TypeReference<>() {
				}, parent1.getId());
		assertThat(children).hasSize(1);
		assertThat(children).element(0).extracting("name").isEqualTo(child2.getName());

		mockMvcRestTemplate.delete(PATH_DETAIL, child1.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, child2.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, parent1.getId());
		mockMvcRestTemplate.delete(PATH_DETAIL, parent2.getId());
	}

	@ComponentScan
	@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
	@EntityScan(basePackageClasses = TestEntity.class)
	static class Config {

	}

}
