package io.cornerstone.core.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

	public static final String DEFAULT_LOGIN_PAGE = "/login.html";
	public static final String DEFAULT_LOGIN_PROCESSING_URL = "/login";
	public static final String DEFAULT_SUCCESS_URL = "/";
	public static final String DEFAULT_LOGOUT_URL = "/logout";

	private String loginPage = DEFAULT_LOGIN_PAGE;

	private String loginProcessingUrl = DEFAULT_LOGIN_PROCESSING_URL;

	private String defaultSuccessUrl = DEFAULT_SUCCESS_URL;

	private String logoutUrl = DEFAULT_LOGOUT_URL;

	private boolean protecting = true;

	private List<String> ignoringPathPatterns = Collections.emptyList();

	private List<String> permitAllPathPatterns = Collections.emptyList();

	private Map<String, String> authorizeRequestsMapping = Collections.emptyMap();

}
