package io.cornerstone.core;

import io.cornerstone.test.SpringApplicationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPropertiesEnvironmentPostProcessorTests extends SpringApplicationTestBase {

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
	void activateOnKubernetesCloudPlatformIsNotUsing() {
		assertThat(this.env.getProperty("mysql.host")).isNull();
	}

	@Test
	void cornerstoneVersionShouldExists() {
		assertThat(this.env.getProperty("cornerstone.version")).isNotBlank();
	}

	@Nested
	@TestPropertySource(properties = "spring.main.cloud-platform=kubernetes")
	class ActiveKubernetesCloudPlatform {

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void cloudPlatformShouldBeActive() {
			assertThat(CloudPlatform.KUBERNETES.isActive(this.env)).isTrue();
		}

		@Test
		void activateOnCloudPlatformIsUsing() {
			// in main/resources/default.yml
			assertThat(this.env.getProperty("mysql.host")).isEqualTo("mysql.default.svc.cluster.local");
		}

		@Test
		void activateOnCloudPlatformShouldNotOverrideApplicationYml() {
			// in test/resources/application.yml
			assertThat(this.env.getProperty("mysql.port")).isEqualTo("3307");
		}

		@Test
		void systemPropertiesShouldOverrideActivateOnCloudPlatform() {
			System.setProperty("mysql.host", "mysql.local");
			assertThat(this.env.getProperty("mysql.host")).isEqualTo("mysql.local");
			System.clearProperty("mysql.host");
		}

	}

	@Nested
	@TestPropertySource(properties = "spring.main.cloud-platform=heroku")
	class OtherActiveCloudPlatform {

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void cloudPlatformShouldBeActive() {
			assertThat(CloudPlatform.HEROKU.isActive(this.env)).isTrue();
		}

		@Test
		void activateOnKubernetesCloudPlatformIsNotUsing() {
			assertThat(this.env.getProperty("mysql.host")).isNull();
		}

	}

}
