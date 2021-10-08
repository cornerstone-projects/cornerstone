package io.cornerstone.core.sequence;

import io.cornerstone.test.containers.Oracle;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Oracle.class)
class OracleSequenceTests extends DatabaseSequenceTests {

}
