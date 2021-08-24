package io.cornerstone.core.web.controller.entity;

import static io.cornerstone.core.web.controller.entity.TestEntityController.PATH_DETAIL;
import static io.cornerstone.core.web.controller.entity.TestEntityController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.cornerstone.test.WebMvcWithDataJpaTestBase;

@ContextConfiguration(classes = EntityControllerTests.Config.class)
class EntityControllerTests extends WebMvcWithDataJpaTestBase {

	@Test
	void crud() throws Exception {
		TestEntity c = new TestEntity();
		c.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c.setName("test");
		c.setDisabled(Boolean.TRUE);

		// create
		TestEntity testEntity = mockMvcRestTemplate.postForObject(PATH_LIST, c, TestEntity.class);
		assertThat(testEntity).isNotNull();
		assertThat(testEntity.getId()).isNotNull();
		assertThat(testEntity.getIdNo()).isEqualTo(c.getIdNo());
		assertThat(testEntity.getName()).isEqualTo(c.getName());
		assertThat(testEntity.getDisabled()).isEqualTo(c.getDisabled());
		Long id = testEntity.getId();

		// read
		assertThat(mockMvcRestTemplate.getForObject(PATH_DETAIL, TestEntity.class, id)).isEqualTo(testEntity);

		// update partial
		TestEntity c2 = new TestEntity();
		c2.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c2.setName("new name");
		c2.setDisabled(Boolean.TRUE);
		TestEntity c3 = mockMvcRestTemplate.patchForObject(PATH_DETAIL, c2, TestEntity.class, id);
		assertThat(c3.getName()).isEqualTo(c2.getName());
		assertThat(c3.getDisabled()).isEqualTo(c2.getDisabled());
		assertThat(c3.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not updatable
		// update full
		c3.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c3.setName("name");
		c3.setDisabled(Boolean.FALSE);
		mockMvcRestTemplate.put(PATH_DETAIL, c3, id);
		TestEntity c4 = mockMvcRestTemplate.getForObject(PATH_DETAIL, TestEntity.class, id);
		assertThat(c4).isNotNull();
		assertThat(c4.getDisabled()).isEqualTo(c3.getDisabled());
		assertThat(c4.getName()).isEqualTo(c3.getName());
		assertThat(c4.getIdNo()).isEqualTo(testEntity.getIdNo()); // idNo not updatable

		// delete
		mockMvcRestTemplate.deleteForResult(PATH_DETAIL, id).andExpect(status().isBadRequest());

		c4.setDisabled(Boolean.TRUE);
		mockMvcRestTemplate.put(PATH_DETAIL, c4, id);
		mockMvcRestTemplate.delete(PATH_DETAIL, id);
		mockMvcRestTemplate.getForResult(PATH_DETAIL, id).andExpect(status().isNotFound());
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
			list.add(mockMvcRestTemplate.postForObject(PATH_LIST, c, TestEntity.class));
		}

		ResultPage<TestEntity> page = mockMvcRestTemplate.getForObject(PATH_LIST, new TypeReference<>() {
		});
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(size);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(size);
		assertThat(page.getResult().get(0).getCreatedDate()).isNull(); // View.List view

		page = mockMvcRestTemplate.getForObject(PATH_LIST + "?page=2&size=1&sort=id,desc", new TypeReference<>() {
		});
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(2);
		assertThat(page.getSize()).isEqualTo(1);

		page = mockMvcRestTemplate.getForObject(PATH_LIST + "?query=test0", new TypeReference<>() {
		});
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);

		ResultPage<TestEntity> page2 = mockMvcRestTemplate.getForObject(PATH_LIST + "?name=test0",
				new TypeReference<>() {
				});
		assertThat(page2).isEqualTo(page);

		for (TestEntity c : list)
			mockMvcRestTemplate.delete(PATH_DETAIL, c.getId());
	}

	@ComponentScan
	@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
	@EntityScan(basePackageClasses = TestEntity.class)
	static class Config {

	}

}
