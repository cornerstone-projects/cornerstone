package io.cornerstone.core.security;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestfulUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final String ATTR_NAME_REQUEST_BODY = "_REQUEST_BODY";

	private final ObjectMapper objectMapper;

	public RestfulUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager,
			ObjectMapper objectMapper) {
		super(authenticationManager);
		this.objectMapper = objectMapper;
	}

	@Nullable
	protected String obtainUsername(HttpServletRequest request) {
		Map<String, String> requestBody = body(request);
		if (requestBody != null) {
			request.setAttribute(ATTR_NAME_REQUEST_BODY, requestBody);
			return requestBody.get(getUsernameParameter());
		}
		return super.obtainUsername(request);
	}

	@Nullable
	protected String obtainPassword(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String> requestBody = (Map<String, String>) request.getAttribute(ATTR_NAME_REQUEST_BODY);
		if (requestBody != null) {
			request.removeAttribute(ATTR_NAME_REQUEST_BODY);
			return requestBody.get(getPasswordParameter());
		}
		return super.obtainPassword(request);
	}

	@Nullable
	private Map<String, String> body(HttpServletRequest request) {
		String contentType = request.getContentType();
		if (contentType != null) {
			if (MediaType.parseMediaType(contentType).isCompatibleWith(APPLICATION_JSON)) {
				try {
					return objectMapper.readValue(request.getInputStream(), new TypeReference<Map<String, String>>() {
					});
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}
}