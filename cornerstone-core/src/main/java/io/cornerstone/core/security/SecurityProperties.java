package io.cornerstone.core.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

	public static final String DEFAULT_LOGIN_PAGE = "/login.html";

	public static final String DEFAULT_LOGIN_PROCESSING_URL = "/login";

	public static final String DEFAULT_SUCCESS_URL = "/";

	public static final String DEFAULT_LOGOUT_URL = "/logout";

	public static final String DEFAULT_TARGET_URL_KEY = "targetUrl";

	private String loginPage = DEFAULT_LOGIN_PAGE;

	private String loginProcessingUrl = DEFAULT_LOGIN_PROCESSING_URL;

	private String defaultSuccessUrl = DEFAULT_SUCCESS_URL;

	private String logoutUrl = DEFAULT_LOGOUT_URL;

	private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;

	private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;

	private String targetUrlParameter = DEFAULT_TARGET_URL_KEY;

	private boolean protecting = true;

	private List<String> ignoringPathPatterns = Collections.emptyList();

	private List<String> permitAllPathPatterns = Collections.emptyList();

	private Map<String, String> authorizeRequestsMapping = Collections.emptyMap();

}
