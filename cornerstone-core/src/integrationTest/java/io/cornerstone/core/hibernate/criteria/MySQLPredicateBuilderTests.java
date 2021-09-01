package io.cornerstone.core.hibernate.criteria;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.MySQL;

@ContextConfiguration(classes = MySQL.class)
class MySQLPredicateBuilderTests extends PredicateBuilderTests {

}
