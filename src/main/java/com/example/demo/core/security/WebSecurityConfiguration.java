package com.example.demo.core.security;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;

import com.example.demo.core.Application;
import com.example.demo.core.util.RequestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, proxyTargetClass = true)
@EnableConfigurationProperties(SecurityProperties.class)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private SecurityProperties properties;

	@Autowired
	private Application application;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void configure(WebSecurity web) {
		List<String> patterns = new ArrayList<>();
		patterns.addAll(Arrays.asList("/error", "/actuator/**", "/assets/**"));
		patterns.addAll(properties.getIgnoringPathPatterns());
		web.ignoring().antMatchers(patterns.toArray(new String[patterns.size()]));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				.antMatchers(properties.getLoginPage(), properties.getLoginProcessingUrl(), properties.getLogoutUrl())
				.permitAll().anyRequest().authenticated().and().exceptionHandling().defaultAuthenticationEntryPointFor(
						new Http403ForbiddenEntryPoint(), RequestUtils::isRequestedFromApi);
		http.formLogin().loginPage(properties.getLoginPage()).loginProcessingUrl(properties.getLoginProcessingUrl())
				.successHandler(authenticationSuccessHandler(http.getSharedObject(RequestCache.class)))
				.failureHandler(authenticationFailureHandler()).and().logout().logoutUrl(properties.getLogoutUrl())
				.logoutSuccessUrl(properties.getLoginPage());
		if (application.isUnitTest()) {
			http.httpBasic();
		}
	}

	AuthenticationSuccessHandler authenticationSuccessHandler(RequestCache requestCache) {
		SavedRequestAwareAuthenticationSuccessHandler defaultSuccessHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		defaultSuccessHandler.setDefaultTargetUrl(properties.getDefaultSuccessUrl());
		if (requestCache != null) {
			defaultSuccessHandler.setRequestCache(requestCache);
		}
		return (request, response, authentication) -> {
			if (RequestUtils.isRequestedFromApi(request)) {
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				Map<String, Object> map = new LinkedHashMap<>();
				map.put("timestamp", new Date());
				map.put("status", HttpStatus.OK.value());
				map.put("message", HttpStatus.OK.getReasonPhrase());
				map.put("path", properties.getLoginProcessingUrl());
				objectMapper.writeValue(response.getWriter(), map);
				// see DefaultErrorAttributes::getErrorAttributes
			} else {
				defaultSuccessHandler.onAuthenticationSuccess(request, response, authentication);
			}
		};
	}

	AuthenticationFailureHandler authenticationFailureHandler() {
		return (request, response, ex) -> {
			if (RequestUtils.isRequestedFromApi(request)) {
				response.sendError(SC_UNAUTHORIZED, ex.getLocalizedMessage());
			} else {
				response.sendRedirect(properties.getLoginPage() + "?error");
				// request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
				// Method Not Allowed for LOGIN_PAGE
			}
		};
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	GrantedAuthorityDefaults grantedAuthorityDefaults() {
		return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
	}

}