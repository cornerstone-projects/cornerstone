package io.cornerstone.core.sequence;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.SQLServer;

@ContextConfiguration(classes = SQLServer.class)
class SQLServerSequenceTests extends DatabaseSequenceTests {

}
