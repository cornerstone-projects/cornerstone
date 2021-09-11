package io.cornerstone.core.sequence;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.Oracle;

@ContextConfiguration(classes = Oracle.class)
class OracleSequenceTests extends DatabaseSequenceTests {

}
