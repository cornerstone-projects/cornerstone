package io.cornerstone.test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringApplicationTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class ControllerMockTestBase extends SpringApplicationTestBase {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	protected MockMvcRestTemplate userRestTemplate() {
		return new MockMvcRestTemplate(mockMvc, objectMapper, httpBasic(USER_USERNAME, DEFAULT_PASSWORD));
	}

	protected MockMvcRestTemplate adminRestTemplate() {
		return new MockMvcRestTemplate(mockMvc, objectMapper, httpBasic(ADMIN_USERNAME, DEFAULT_PASSWORD));
	}

}
