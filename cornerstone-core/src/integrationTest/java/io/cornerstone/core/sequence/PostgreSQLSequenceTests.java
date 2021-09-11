package io.cornerstone.core.sequence;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.PostgreSQL;

@ContextConfiguration(classes = PostgreSQL.class)
class PostgreSQLSequenceTests extends DatabaseSequenceTests {

}
