package io.cornerstone.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.function.Function;
import java.util.function.Supplier;

import io.cornerstone.test.SpringApplicationTestBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import static io.cornerstone.core.SecretPropertiesPostProcessor.KEY_DECODER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class SecretPropertiesPostProcessorTests extends SpringApplicationTestBase {

	static File configFile;

	@BeforeAll
	static void init() throws IOException {
		configFile = new File(System.getProperty("user.dir"), "secret.properties");
		String content = "foo=" + Base64.getEncoder().encodeToString("bar".getBytes());
		content += "\nmysql.port=" + Base64.getEncoder().encodeToString("33060".getBytes());
		Files.write(configFile.toPath(), content.getBytes());
	}

	@AfterAll
	static void cleanup() {
		configFile.delete();
	}

	@Autowired
	private ConfigurableEnvironment env;

	@Test
	void secretPropertiesPropertySourceIsRegistered() {
		assertThat(this.env.getProperty("foo")).isEqualTo("bar");
	}

	@Test
	void secretPropertiesShouldOverrideApplicationProperties() {
		assertThat(this.env.getProperty("mysql.port")).isEqualTo("33060");
	}

	@Test
	void decoderIsNotFound() throws Exception {
		String content = KEY_DECODER + "=com.example.MyDecoder\nfoo=bar";
		Files.write(configFile.toPath(), content.getBytes());
		assertThatIllegalArgumentException().isThrownBy(() -> {
			createProcessor().postProcessEnvironment(mock(ConfigurableEnvironment.class), null);
		}).withMessageContaining("not found");
	}

	@Test
	void decoderIsInvalid() throws Exception {
		String content = KEY_DECODER + "=" + InvalidDecoder.class.getName() + "\nfoo=bar";
		Files.write(configFile.toPath(), content.getBytes());
		assertThatIllegalArgumentException().isThrownBy(() -> {
			createProcessor().postProcessEnvironment(mock(ConfigurableEnvironment.class), null);
		}).withMessageContaining("java.util.function.Function<String,String>");
	}

	@Nested
	class ExplicitDecoder {

		static File configFile;

		@BeforeAll
		static void init() throws IOException {
			configFile = new File(System.getProperty("user.dir"), "secret.properties");
			String content = KEY_DECODER + "=" + DummyDecoder.class.getName() + "\nfoo=bar";
			Files.write(configFile.toPath(), content.getBytes());
		}

		@AfterAll
		static void cleanup() {
			configFile.delete();
		}

		@Autowired
		private ConfigurableEnvironment env;

		@Test
		void decoderIsDefined() {
			assertThat(this.env.getProperty("foo")).isEqualTo("dummybar");
		}

	}

	private static SecretPropertiesPostProcessor createProcessor() {
		return new SecretPropertiesPostProcessor(new DeferredLogFactory() {
			@Override
			public Log getLog(Supplier<Log> destination) {
				return LogFactory.getLog(getClass());
			}
		});
	}

	public static class InvalidDecoder implements Function<String, Long> {

		@Override
		public Long apply(String t) {
			return 0L;
		}

	}

	public static class DummyDecoder implements Function<String, String> {

		@Override
		public String apply(String t) {
			return "dummy" + t;
		}

	}

}
