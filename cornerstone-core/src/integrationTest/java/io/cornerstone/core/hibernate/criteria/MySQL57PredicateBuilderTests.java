package io.cornerstone.core.hibernate.criteria;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import io.cornerstone.test.containers.MySQL;

@ContextConfiguration(classes = MySQL.class)
@TestPropertySource(properties = { MySQL.IMAGE + "=mysql:5.7", MySQL.INIT_SQL
		+ "=create function regexp_like (text varchar(255), pattern varchar(255)) returns integer deterministic return text regexp pattern;" })
class MySQL57PredicateBuilderTests extends PredicateBuilderTests {

}
