package io.cornerstone.test;

import io.cornerstone.core.web.DefaultWebMvcConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@WebMvcTest
@ContextConfiguration
@WithMockUser
@ActiveProfiles("test")
public abstract class WebMvcTestBase {

	protected MockMvc mockMvc;

	protected RestTemplate restTemplate;

	@Autowired
	@SuppressWarnings("deprecation")
	private void setMockMvc(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
		this.restTemplate = new RestTemplate(new MockMvcClientHttpRequestFactory(this.mockMvc));
	}

	@Configuration
	@ComponentScan(basePackageClasses = DefaultWebMvcConfigurer.class,
			excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
					@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
	static class Config {

		@Bean
		SecurityFilterChain filterChain(HttpSecurity http) {
			return http.csrf(CsrfConfigurer::disable).build();
		}

	}

}
