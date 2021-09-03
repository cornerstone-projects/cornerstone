package io.cornerstone.core.hibernate.criteria;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import io.cornerstone.test.containers.PostgreSQL;

@ContextConfiguration(classes = PostgreSQL.class)
class PostgreSQLPredicateBuilderTests extends PredicateBuilderTests {

	@Sql(statements = "create or replace function regexp_like(character varying,character varying) returns integer as ' select ($1 ~ $2)::int; ' language sql immutable;")
	@Test
	void testRegexpLike() {
		super.testRegexpLike();
	}

}
