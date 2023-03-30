package io.cornerstone.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import io.cornerstone.test.SpringApplicationTestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.TestPropertySource;

import static io.cornerstone.core.DefaultPropertiesPostProcessor.SYSTEM_PROPERTY_CONFIG_DIR;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultPropertiesPostProcessorTests extends SpringApplicationTestBase {

	@Autowired
	private Environment env;

	@Test
	void defaultYmlShouldOverrideSpringBootDefaults() {
		assertThat(this.env.getProperty("server.error.include-message")).isEqualTo("always");
	}

	@Test
	void applicationYmlShouldOverrideDefaultYml() {
		assertThat(this.env.getProperty("server.error.include-binding-errors")).isEqualTo("never");
	}

	@Test
	void defaultYmlWithTestProfileShouldOverrideSpringBootDefaults() {
		assertThat(this.env.getProperty("spring.jackson.serialization.indent_output")).isEqualTo("true");
	}

	@Test
	void applicationYmlShouldOverrideDefaultYmlWithTestProfile() {
		assertThat(this.env.getProperty("spring.springdoc.swagger-ui.enabled")).isEqualTo("false");
	}

	@Test
	void applicationYmlWithTestProfileShouldOverrideDefaultYmlWithTestProfile() {
		assertThat(this.env.getProperty("spring.jpa.hbm2ddl.default_constraint_mode")).isEqualTo("CONSTRAINT");
	}

	@Test
	void cornerstoneVersionShouldExists() {
		assertThat(this.env.getProperty("cornerstone.version")).isNotBlank();
	}

	@Nested
	@TestPropertySource(properties = "spring.main.cloud-platform=kubernetes")
	class KubernetesCloudPlatform {

		static Path foobar;

		@BeforeAll
		static void init() throws IOException {
			String configDir = System.getProperty("java.io.tmpdir") + "/etc/config/";
			System.setProperty(SYSTEM_PROPERTY_CONFIG_DIR, configDir);
			foobar = Path.of(configDir, "foo", "bar");
			if (!Files.exists(foobar)) {
				Files.createDirectories(foobar.getParent());
				Files.write(foobar, "foobar".getBytes());
			}
		}

		@AfterAll
		static void cleanup() throws IOException {
			Files.delete(foobar);
			Files.delete(foobar.getParent());
			Files.delete(foobar.getParent().getParent());
			Files.delete(foobar.getParent().getParent().getParent());
		}

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void configDirPropertySourceIsRegistered() {
			assertThat(this.env.getProperty("foo.bar")).isEqualTo("foobar");
		}

		@Test
		void cloudPlatformShouldBeActive() {
			assertThat(CloudPlatform.KUBERNETES.isActive(this.env));
		}

		@Test
		void defaultYmlWithCloudPlatformShouldOverrideApplicationYml() {
			assertThat(this.env.getProperty("mysql.host")).isEqualTo("mysql.default.svc.cluster.local");
		}

		@Test
		void systemPropertiesShouldOverrideDefaultYmlWithCloudPlatform() {
			System.setProperty("mysql.host", "mysql.local");
			assertThat(this.env.getProperty("mysql.host")).isEqualTo("mysql.local");
			System.clearProperty("mysql.host");
			assertThat(this.env.getProperty("mysql.host")).isEqualTo("mysql.default.svc.cluster.local");
		}

		@Test
		void defaultYmlWithCloudPlatformShouldBetweenSystemPropertiesAndSystemEnvironment() {
			int posistionOfClouldPlatform = positionOf(ps -> ps.getName().startsWith("default.yml ")
					&& ps.getProperty("spring.config.activate.on-cloud-platform").equals("kubernetes"));
			int positionOfSystemProperties = positionOf(
					ps -> ps.getName().equals(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME));
			int positionOfSystemEnvironment = positionOf(
					ps -> ps.getName().equals(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME));
			assertThat(posistionOfClouldPlatform).isGreaterThanOrEqualTo(0);
			assertThat(posistionOfClouldPlatform).isBetween(positionOfSystemProperties, positionOfSystemEnvironment);
		}

		private int positionOf(Predicate<PropertySource<?>> predicate) {
			MutablePropertySources propertySources = this.env.getPropertySources();
			int position = 0;
			for (PropertySource<?> ps : propertySources) {
				if (predicate.test(ps)) {
					return position;
				}
				position++;
			}
			return -1;
		}

	}

}
