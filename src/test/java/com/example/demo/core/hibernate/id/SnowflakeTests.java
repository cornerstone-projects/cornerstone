package com.example.demo.core.hibernate.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.SpringApplicationTestBase;

public class SnowflakeTests extends SpringApplicationTestBase {

	@Autowired
	SimpleEntityRepository repository;

	@Autowired
	SnowflakeProperties snowflakeProperties;

	@Test
	public void test() {
		Long id1 = repository.save(new SimpleEntity()).getId();
		Long id2 = repository.save(new SimpleEntity()).getId();
		assertThat(id1).isGreaterThan(100000000);
		assertThat(id2).isGreaterThan(id1);
		Snowflake sf = snowflakeProperties.build();
		assertThat(sf.parse(id1).getWorkerId()).isEqualTo(sf.parse(id2).getWorkerId());
		assertThat(sf.parse(id1).getTimestamp()).isLessThanOrEqualTo(sf.parse(id2).getTimestamp());
	}

}
