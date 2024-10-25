package io.cornerstone.core.persistence.criteria;

import io.cornerstone.test.containers.UseSQLServerContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@UseSQLServerContainer
class SQLServerPredicateBuilderTests extends PredicateBuilderTests {

	@Disabled("Regexp is not natively supported by SQL Server")
	@Test
	@Override
	void testRegexpLike() {
		super.testRegexpLike();
	}

}
