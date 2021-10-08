package io.cornerstone.core.sequence;

import io.cornerstone.test.containers.MySQL;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = MySQL.class)
class MySQLSequenceTests extends DatabaseSequenceTests {

}
