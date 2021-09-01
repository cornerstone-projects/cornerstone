package io.cornerstone.core.sequence;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.MySQL;

@ContextConfiguration(classes = MySQL.class)
public class MySQLSequenceTests extends DatabaseSequenceTests {

}
