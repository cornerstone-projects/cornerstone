package io.cornerstone.core.hibernate.criteria;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import io.cornerstone.test.containers.MySQL57;

@ContextConfiguration(classes = MySQL57.class)
class MySQL57PredicateBuilderTests extends PredicateBuilderTests {

	@Sql(statements = "create function regexp_like (text varchar(255), pattern varchar(255)) returns integer deterministic return text regexp pattern;")
	@Test
	void testRegexpLike() {
		super.testRegexpLike();
	}

}
