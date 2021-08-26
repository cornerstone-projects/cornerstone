package io.cornerstone.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import io.cornerstone.core.web.DefaultWebMvcRegistrations;

@WebMvcTest
@ContextConfiguration(classes = WebMvcTestBase.Config.class)
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@Import(DefaultWebMvcRegistrations.class)
@ActiveProfiles("test")
public abstract class WebMvcTestBase {

	protected MockMvc mockMvc;

	protected RestTemplate restTemplate;

	@Autowired
	private void setMockMvc(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
		this.restTemplate = new RestTemplate(new MockMvcClientHttpRequestFactory(this.mockMvc));
	}

	@ComponentScan(basePackageClasses = DefaultWebMvcRegistrations.class, excludeFilters = {
			@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
			@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
	static class Config {

	}
}
