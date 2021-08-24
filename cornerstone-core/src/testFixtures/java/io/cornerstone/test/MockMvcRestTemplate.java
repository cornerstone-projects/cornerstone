package io.cornerstone.test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class MockMvcRestTemplate {

	private final MockMvc mockMvc;

	private final ObjectMapper objectMapper;

	private RequestPostProcessor requestPostProcessor = r -> r;

	public MockMvcRestTemplate with(RequestPostProcessor requestPostProcessor) {
		return new MockMvcRestTemplate(mockMvc, objectMapper, requestPostProcessor);
	}

	public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url, uriVariables).with(requestPostProcessor))
				.andExpect(status().is2xxSuccessful()).andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
	}

	public <T> T getForObject(String url, TypeReference<T> type, Object... uriVariables) throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url, uriVariables).with(requestPostProcessor))
				.andExpect(status().is2xxSuccessful()).andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), type);
	}

	public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
			throws Exception {
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.post(url, uriVariables).with(requestPostProcessor)
						.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
				.andExpect(status().is2xxSuccessful()).andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
	}

	public void put(String url, Object request, Object... uriVariables) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put(url, uriVariables).with(requestPostProcessor)
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
				.andExpect(status().is2xxSuccessful());
	}

	public <T> T patchForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
			throws Exception {
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.patch(url, uriVariables).with(requestPostProcessor)
						.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
				.andExpect(status().is2xxSuccessful()).andReturn();
		return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
	}

	public void delete(String url, Object... uriVariables) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete(url, uriVariables).with(requestPostProcessor))
				.andExpect(status().is2xxSuccessful());
	}

	public ResultActions getForResult(String url, Object... uriVariables) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get(url, uriVariables).with(requestPostProcessor));
	}

	public ResultActions postForResult(String url, Object request, Object... uriVariables) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.post(url, uriVariables).with(requestPostProcessor)
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)));
	}

	public ResultActions putForResult(String url, Object request, Object... uriVariables) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.put(url, uriVariables).with(requestPostProcessor)
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)));
	}

	public ResultActions patchForResult(String url, Object request, Object... uriVariables) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.patch(url, uriVariables).with(requestPostProcessor)
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)));
	}

	public ResultActions deleteForResult(String url, Object... uriVariables) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.delete(url, uriVariables).with(requestPostProcessor));
	}
}
