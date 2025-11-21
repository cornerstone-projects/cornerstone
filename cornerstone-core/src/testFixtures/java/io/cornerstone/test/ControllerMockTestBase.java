package io.cornerstone.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringApplicationTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class ControllerMockTestBase extends SpringApplicationTestBase {

	@Autowired
	protected MockMvc mockMvc;

	protected RestTemplate userRestTemplate() {
		return getRestTemplate(USER_USERNAME, DEFAULT_PASSWORD);
	}

	protected RestTemplate adminRestTemplate() {
		return getRestTemplate(ADMIN_USERNAME, DEFAULT_PASSWORD);
	}

	@SuppressWarnings("deprecation")
	protected RestTemplate getRestTemplate(String username, String password) {
		RestTemplate mockMvcRestTemplate = new RestTemplate(new MockMvcClientHttpRequestFactory(this.mockMvc));
		mockMvcRestTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
		return mockMvcRestTemplate;
	}

}
