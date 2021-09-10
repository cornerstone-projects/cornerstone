package io.cornerstone.core.hibernate.criteria;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.test.containers.SQLServer;

@ContextConfiguration(classes = SQLServer.class)
class SQLServerPredicateBuilderTests extends PredicateBuilderTests {

	@Override
	@Disabled
	@Test
	void testRegexpLike() {
		super.testRegexpLike();
	}
}
