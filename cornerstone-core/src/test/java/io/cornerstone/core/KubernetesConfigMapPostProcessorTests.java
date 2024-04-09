package io.cornerstone.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import io.cornerstone.test.SpringApplicationTestBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.FileSystemUtils;

import static io.cornerstone.core.KubernetesConfigMapPostProcessor.SYSTEM_PROPERTY_CONFIG_MAP_DIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.main.cloud-platform=kubernetes")
class KubernetesConfigMapPostProcessorTests extends SpringApplicationTestBase {

	static Path configMapDir;

	@BeforeAll
	static void init() throws IOException {
		String dir = (System.getProperty("java.io.tmpdir") + "/etc/config/");
		System.setProperty(SYSTEM_PROPERTY_CONFIG_MAP_DIR, dir);
		configMapDir = Path.of(dir);
		createFile("foobar", dir, "foo", "bar");
		createFile("myhost", dir, "mysql", "host");
	}

	@AfterAll
	static void cleanup() throws IOException {
		FileSystemUtils.deleteRecursively(configMapDir);
		System.clearProperty(SYSTEM_PROPERTY_CONFIG_MAP_DIR);
	}

	@Autowired
	private ConfigurableEnvironment env;

	@Test
	void cloudPlatformShouldBeActive() {
		assertThat(CloudPlatform.KUBERNETES.isActive(this.env));
	}

	@Test
	void configTreePropertySourceIsRegistered() {
		assertThat(this.env.getProperty("foo.bar")).isEqualTo("foobar");
	}

	@Test
	void configTreePropertySourceShouldOverrideConfigData() {
		assertThat(this.env.getProperty("mysql.host")).isEqualTo("myhost");
	}

	@Test
	void configTreePropertySourceShouldNotOverrideSystemProperties() {
		System.setProperty("foo.bar", "foobarFromSystemProperties");
		assertThat(this.env.getProperty("foo.bar")).isEqualTo("foobarFromSystemProperties");
		System.clearProperty("foo.bar");
	}

	@Test
	void shouldThrowExceptionIfConfigMapDirDoesNotExist() {
		System.setProperty(SYSTEM_PROPERTY_CONFIG_MAP_DIR, "/not/existing/dir");
		assertThatExceptionOfType(ConfigDataLocationNotFoundException.class)
			.isThrownBy(() -> createProcessor().postProcessEnvironment(createKubernetesEnvironment(), null));
		System.clearProperty(SYSTEM_PROPERTY_CONFIG_MAP_DIR);
	}

	@Test
	void shouldNotThrowExceptionIfOptionalConfigMapDirDoesNotExist() {
		System.setProperty(SYSTEM_PROPERTY_CONFIG_MAP_DIR, "optional:/not/existing/dir");
		createProcessor().postProcessEnvironment(createKubernetesEnvironment(), null);
		System.clearProperty(SYSTEM_PROPERTY_CONFIG_MAP_DIR);
	}

	private static void createFile(String content, String dir, String... subs) throws IOException {
		Path path = Path.of(dir, subs);
		if (!Files.exists(path)) {
			Files.createDirectories(path.getParent());
			Files.write(path, content.getBytes());
		}
	}

	private static KubernetesConfigMapPostProcessor createProcessor() {
		return new KubernetesConfigMapPostProcessor(new DeferredLogFactory() {
			@Override
			public Log getLog(Supplier<Log> destination) {
				return LogFactory.getLog(getClass());
			}
		});
	}

	private static ConfigurableEnvironment createKubernetesEnvironment() {
		ConfigurableEnvironment environment = mock();
		given(environment.getProperty("spring.main.cloud-platform")).willReturn("kubernetes");
		return environment;
	}

}
