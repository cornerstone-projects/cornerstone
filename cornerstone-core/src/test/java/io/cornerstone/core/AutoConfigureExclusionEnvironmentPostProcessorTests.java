package io.cornerstone.core;

import io.cornerstone.test.SpringApplicationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;

import static io.cornerstone.core.AutoConfigureExclusionEnvironmentPostProcessor.*;
import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigureExclusionEnvironmentPostProcessorTests extends SpringApplicationTestBase {

	private static final String FOO_AUTO_CONFIGURATION = "com.example.FooAutoConfiguration";

	private static final String BAR_AUTO_CONFIGURATION = "com.example.BarAutoConfiguration";

	@Nested
	@TestPropertySource(properties = PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE + "=" + FOO_AUTO_CONFIGURATION)
	class ExcludeOnly {

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void test() {
			assertThat(this.env.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class))
				.containsOnly(FOO_AUTO_CONFIGURATION);
		}

	}

	@Nested
	@TestPropertySource(properties = PROPERTY_NAME_AUTOCONFIGURE_EXCLUSIONS + "." + FOO_AUTO_CONFIGURATION + "=true")
	class ExclusionOnly {

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void test() {
			assertThat(this.env.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class))
				.contains(FOO_AUTO_CONFIGURATION);
		}

	}

	@Nested
	@TestPropertySource(properties = { PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE + "=" + FOO_AUTO_CONFIGURATION,
			PROPERTY_NAME_AUTOCONFIGURE_EXCLUSIONS + "." + BAR_AUTO_CONFIGURATION + "=true" })
	class ExcludeAndExclusion {

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void test() {
			assertThat(this.env.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class))
				.containsOnly(FOO_AUTO_CONFIGURATION, BAR_AUTO_CONFIGURATION);
		}

	}

	@Nested
	@TestPropertySource(properties = {
			PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE + "=" + FOO_AUTO_CONFIGURATION + "," + BAR_AUTO_CONFIGURATION,
			PROPERTY_NAME_AUTOCONFIGURE_EXCLUSIONS + "." + FOO_AUTO_CONFIGURATION + "=false" })
	class ExclusionOverrideExclude {

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void test() {
			assertThat(this.env.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class))
				.containsOnly(BAR_AUTO_CONFIGURATION);
		}

	}

}
