package io.cornerstone.core.hibernate.criteria;

import io.cornerstone.test.containers.PostgreSQL;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

@ContextConfiguration(classes = PostgreSQL.class)
class PostgreSQLPredicateBuilderTests extends PredicateBuilderTests {

	@Override
	@Sql(statements = "create or replace function regexp_like(character varying,character varying) returns integer as ' select ($1 ~ $2)::int; ' language sql immutable;")
	@Test
	void testRegexpLike() {
		super.testRegexpLike();
	}

}
