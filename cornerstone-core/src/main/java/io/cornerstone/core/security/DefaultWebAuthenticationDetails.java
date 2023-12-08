package io.cornerstone.core.security;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class DefaultWebAuthenticationDetails extends WebAuthenticationDetails {

	public static final String PARAMETER_NAME_VERIFICATION_CODE = "verificationCode";

	private static final long serialVersionUID = -1899435340020810774L;

	private final String verificationCode;

	public DefaultWebAuthenticationDetails(HttpServletRequest request, Map<String, String> requestBody) {
		super(request);
		if (requestBody != null) {
			this.verificationCode = requestBody.get(PARAMETER_NAME_VERIFICATION_CODE);
		}
		else {
			this.verificationCode = request.getParameter(PARAMETER_NAME_VERIFICATION_CODE);
		}
	}

	public String getVerificationCode() {
		return this.verificationCode;
	}

}
