package io.cornerstone.core.sequence;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.Oracle;

@ContextConfiguration(classes = Oracle.class)
public class OracleSequenceTests extends DatabaseSequenceTests {

}
