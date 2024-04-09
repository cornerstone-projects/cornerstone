package io.cornerstone.core.security;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
public class RestfulUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final String ATTR_NAME_REQUEST_BODY = "_REQUEST_BODY";

	private final ObjectMapper objectMapper;

	public RestfulUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		setAuthenticationDetailsSource(new WebAuthenticationDetailsSource() {

			@Override
			public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
				return new DefaultWebAuthenticationDetails(context, body(context));
			}

		});
	}

	@Override
	@Nullable
	protected String obtainUsername(HttpServletRequest request) {
		Map<String, String> requestBody = body(request);
		if (requestBody != null) {
			return requestBody.get(getUsernameParameter());
		}
		return super.obtainUsername(request);
	}

	@Override
	@Nullable
	protected String obtainPassword(HttpServletRequest request) {
		Map<String, String> requestBody = body(request);
		if (requestBody != null) {
			return requestBody.get(getPasswordParameter());
		}
		return super.obtainPassword(request);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private Map<String, String> body(HttpServletRequest request) {
		Map<String, String> requestBody = (Map<String, String>) request.getAttribute(ATTR_NAME_REQUEST_BODY);
		if (requestBody != null) {
			return requestBody;
		}
		String contentType = request.getContentType();
		if (contentType != null) {
			if (MediaType.parseMediaType(contentType).isCompatibleWith(APPLICATION_JSON)) {
				try {
					requestBody = this.objectMapper.readValue(request.getInputStream(), new TypeReference<>() {
					});
					request.setAttribute(ATTR_NAME_REQUEST_BODY, requestBody);
					return requestBody;
				}
				catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		return null;
	}

}
