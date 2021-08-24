package io.cornerstone.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ActiveProfiles({ "test", "WebMvcTest" })
public abstract class WebMvcTestBase {

	protected MockMvc mockMvc;

	protected ObjectMapper objectMapper;

	protected MockMvcRestTemplate mockMvcRestTemplate;

	@Autowired
	private void init(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
		this.mockMvcRestTemplate = new MockMvcRestTemplate(mockMvc, objectMapper);
	}

}
