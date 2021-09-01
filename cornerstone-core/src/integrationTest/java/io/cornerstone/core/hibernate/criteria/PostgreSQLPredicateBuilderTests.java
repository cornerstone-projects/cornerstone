package io.cornerstone.core.hibernate.criteria;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import io.cornerstone.test.containers.PostgreSQL;

@ContextConfiguration(classes = PostgreSQL.class)
@TestPropertySource(properties = PostgreSQL.INIT_SQL
		+ "=create or replace function regexp_like(character varying,character varying) returns integer as $$ select ($1 ~ $2)::int; $$ language sql immutable;")
class PostgreSQLPredicateBuilderTests extends PredicateBuilderTests {

}
