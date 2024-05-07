package io.cornerstone.test;

import io.cornerstone.core.web.DefaultWebMvcConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@WebMvcTest
@ContextConfiguration
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
public abstract class WebMvcTestBase {

	protected MockMvc mockMvc;

	protected RestTemplate restTemplate;

	@Autowired
	private void setMockMvc(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
		this.restTemplate = new RestTemplate(new MockMvcClientHttpRequestFactory(this.mockMvc));
	}

	@Configuration
	@ComponentScan(basePackageClasses = DefaultWebMvcConfigurer.class,
			excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
					@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
	static class Config {

	}

}
