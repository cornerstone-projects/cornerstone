package io.cornerstone.core.sequence;

import io.cornerstone.test.containers.PostgreSQL;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = PostgreSQL.class)
class PostgreSQLSequenceTests extends DatabaseSequenceTests {

}
