package io.cornerstone.core.sequence;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.Db2;

@ContextConfiguration(classes = Db2.class)
class Db2SequenceTests extends DatabaseSequenceTests {

}
