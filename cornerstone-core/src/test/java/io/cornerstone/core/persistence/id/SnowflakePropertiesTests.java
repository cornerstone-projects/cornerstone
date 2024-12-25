package io.cornerstone.core.persistence.id;

import io.cornerstone.core.Application;
import io.cornerstone.core.DefaultApplication;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakePropertiesTests {

	private static ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class));

	@Test
	void testWorkIdFromHostAddress() {
		runner.withUserConfiguration(Config.class).run(ctx -> {
			SnowflakeProperties properties = ctx.getBean(SnowflakeProperties.class);
			assertThat(properties.getWorkerId()).isEqualTo(25);
		});
	}

	@Test
	void testWorkIdFromHostName() {
		runner.withUserConfiguration(Config.class)
			.withPropertyValues("spring.main.cloud-platform=kubernetes")
			.run(ctx -> {
				SnowflakeProperties properties = ctx.getBean(SnowflakeProperties.class);
				assertThat(properties.getWorkerId()).isEqualTo(20);
			});
	}

	@Test
	void testConfiguredWorkId() {
		runner.withUserConfiguration(Config.class)
			.withPropertyValues("spring.main.cloud-platform=kubernetes", "snowflake.worker-id=0")
			.run(ctx -> {
				SnowflakeProperties properties = ctx.getBean(SnowflakeProperties.class);
				assertThat(properties.getWorkerId()).isEqualTo(0);
			});
	}

	@EnableConfigurationProperties(SnowflakeProperties.class)
	static class Config {

		@Bean
		Application application(ApplicationContext context) {
			return new DefaultApplication() {
				@Override
				public ApplicationContext getContext() {
					return context;
				}

				@Override
				public String getHostAddress() {
					return "192.168.4.25";
				}

				@Override
				public String getHostName() {
					return "application-20";
				}
			};
		}

	}

}
