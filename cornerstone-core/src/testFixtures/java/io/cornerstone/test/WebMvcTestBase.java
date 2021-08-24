package io.cornerstone.test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ActiveProfiles({ "test", "WebMvcTest" })
public abstract class WebMvcTestBase {

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected MockMvc mockMvc;

	protected <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url, uriVariables)).andExpect(status().isOk())
				.andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
	}

	protected <T> T getForObject(String url, TypeReference<T> type, Object... uriVariables) throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url, uriVariables)).andExpect(status().isOk())
				.andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), type);
	}

	public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
			throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(url, uriVariables).contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk()).andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
	}

	protected void put(String url, Object request, Object... uriVariables) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put(url, uriVariables).contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	public <T> T patchForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
			throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(url, uriVariables).contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk()).andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
	}

	protected void delete(String url, Object... uriVariables) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete(url, uriVariables)).andExpect(status().isOk());
	}

	public void getAndExpect(String url, ResultMatcher resultMatcher, Object... uriVariables) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(url, uriVariables)).andExpect(resultMatcher);
	}

	public void postAndExpect(String url, Object request, ResultMatcher resultMatcher, Object... uriVariables)
			throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(url, uriVariables).contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(resultMatcher);
	}

	public void putAndExpect(String url, Object request, ResultMatcher resultMatcher, Object... uriVariables)
			throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put(url, uriVariables).contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(resultMatcher);
	}

	public void patchAndExpect(String url, Object request, ResultMatcher resultMatcher, Object... uriVariables)
			throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.patch(url, uriVariables).contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(resultMatcher);
	}

	public void deleteAndExpect(String url, ResultMatcher resultMatcher, Object... uriVariables) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete(url, uriVariables)).andExpect(resultMatcher);
	}
}
