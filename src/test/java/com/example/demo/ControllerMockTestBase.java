package com.example.demo;

import static com.example.demo.MainApplication.ADMIN_USERNAME;
import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.MainApplication.USER_USERNAME;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringApplicationTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class ControllerMockTestBase {

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected MockMvc mockMvc;

	protected String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	protected RequestPostProcessor user() {
		return httpBasic(USER_USERNAME, DEFAULT_PASSWORD);
	}

	protected RequestPostProcessor admin() {
		return httpBasic(ADMIN_USERNAME, DEFAULT_PASSWORD);
	}

}
