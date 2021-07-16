package com.example.demo.core.hibernate.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.demo.SpringApplicationTest;

@SpringApplicationTest(webEnvironment = WebEnvironment.NONE)
public class JsonTypeTests {

	@Autowired
	DummyEntityRepository repository;

	@Test
	public void test() {
		DummyEntity entity = new DummyEntity();
		entity.setDummyComponentList(Arrays.asList(new DummyComponent("a", 1, new BigDecimal("10.1")),
				new DummyComponent("b", 2, new BigDecimal("10.2")),
				new DummyComponent("c", 3, new BigDecimal("10.3"))));
		repository.save(entity);
		DummyEntity savedEntity = repository.findById(entity.getId()).orElseThrow(IllegalStateException::new);
		assertThat(savedEntity).isNotSameAs(entity);
		assertThat(savedEntity).isEqualTo(entity);
	}

}
