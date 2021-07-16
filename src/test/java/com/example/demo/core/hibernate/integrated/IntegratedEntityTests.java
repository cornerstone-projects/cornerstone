package com.example.demo.core.hibernate.integrated;

import static com.example.demo.core.hibernate.integrated.IntegratedEntity.TestEnum.A;
import static com.example.demo.core.hibernate.integrated.IntegratedEntity.TestEnum.B;
import static com.example.demo.core.hibernate.integrated.IntegratedEntity.TestEnum.C;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.demo.SpringApplicationTest;

@SpringApplicationTest(webEnvironment = WebEnvironment.NONE)
public class IntegratedEntityTests {

	@Autowired
	IntegratedEntityRepository repository;

	@Test
	public void test() {
		IntegratedEntity entity = new IntegratedEntity();
		entity.setStringArray(new String[] { "a", "b", "c" });
		entity.setStringList(Arrays.asList(entity.getStringArray()));
		entity.setStringSet(new LinkedHashSet<>(entity.getStringList()));
		entity.setStringMap(Collections.singletonMap("key", "value"));
		entity.setIntegerArray(new Integer[] { 1, 2, 3 });
		entity.setIntegerList(Arrays.asList(entity.getIntegerArray()));
		entity.setIntegerSet(new LinkedHashSet<>(entity.getIntegerList()));
		entity.setLongArray(new Long[] { 1L, 2L, 3L });
		entity.setLongList(Arrays.asList(entity.getLongArray()));
		entity.setLongSet(new LinkedHashSet<>(entity.getLongList()));
		entity.setEnumArray(new IntegratedEntity.TestEnum[] { A, B, C });
		entity.setEnumList(Arrays.asList(entity.getEnumArray()));
		entity.setEnumSet(new LinkedHashSet<>(entity.getEnumList()));
		entity.setTestComponentList(Arrays.asList(new IntegratedEntity.TestComponent("a", 1, new BigDecimal("10.1")),
				new IntegratedEntity.TestComponent("b", 2, new BigDecimal("10.2")),
				new IntegratedEntity.TestComponent("c", 3, new BigDecimal("10.3"))));
		entity.setAnotherComponentList(Arrays.asList(new IntegratedEntity.AnotherComponent("a", 1, new BigDecimal("10.1")),
				new IntegratedEntity.AnotherComponent("b", 2, new BigDecimal("10.2")),
				new IntegratedEntity.AnotherComponent("c", 3, new BigDecimal("10.3"))));
		repository.save(entity);
		assertThat(entity.getId()).isGreaterThan(100000000L);
		IntegratedEntity savedEntity = repository.findById(entity.getId()).orElseThrow(IllegalStateException::new);
		assertThat(savedEntity).isNotSameAs(entity);
		assertThat(savedEntity).isEqualTo(entity);

	}

}
