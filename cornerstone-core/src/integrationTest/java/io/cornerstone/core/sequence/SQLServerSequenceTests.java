package io.cornerstone.core.sequence;

import io.cornerstone.test.containers.SQLServer;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = SQLServer.class)
class SQLServerSequenceTests extends DatabaseSequenceTests {

}
