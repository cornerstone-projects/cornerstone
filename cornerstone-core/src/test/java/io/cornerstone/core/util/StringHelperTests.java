package io.cornerstone.core.util;

import org.junit.jupiter.api.Test;

import static io.cornerstone.core.util.StringHelper.pluralOf;
import static org.assertj.core.api.Assertions.assertThat;

class StringHelperTests {

	@Test
	void testPluralOf() {
		assertThat(pluralOf(null)).isNull();
		assertThat(pluralOf("")).isEqualTo("");
		assertThat(pluralOf("man")).isEqualTo("men");
		assertThat(pluralOf("testMan")).isEqualTo("testMen");
		assertThat(pluralOf("test_man")).isEqualTo("test_men");
		assertThat(pluralOf("person")).isEqualTo("persons");
		assertThat(pluralOf("testPerson")).isEqualTo("testPersons");
		assertThat(pluralOf("test_person")).isEqualTo("test_persons");
		assertThat(pluralOf("bus")).isEqualTo("buses");
		assertThat(pluralOf("testBus")).isEqualTo("testBuses");
		assertThat(pluralOf("test_bus")).isEqualTo("test_buses");
		assertThat(pluralOf("entity")).isEqualTo("entities");
		assertThat(pluralOf("testEntity")).isEqualTo("testEntities");
		assertThat(pluralOf("test_entity")).isEqualTo("test_entities");
	}

}
