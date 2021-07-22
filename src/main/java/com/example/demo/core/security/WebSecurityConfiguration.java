package com.example.demo.core.security;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.RequestCache;

import com.example.demo.core.Application;
import com.example.demo.core.util.RequestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
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
		List<String> ignoringPathPatterns = new ArrayList<>();
		ignoringPathPatterns.addAll(Arrays.asList("/error", "/actuator/**", "/assets/**"));
		ignoringPathPatterns.addAll(properties.getIgnoringPathPatterns());
		web.ignoring().antMatchers(ignoringPathPatterns.toArray(new String[ignoringPathPatterns.size()]));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String[] permitAllPathPatterns;
		if (properties.isProtecting()) {
			List<String> patterns = new ArrayList<>();
			patterns.addAll(Arrays.asList(properties.getLoginPage(), properties.getLoginProcessingUrl(),
					properties.getLogoutUrl()));
			patterns.addAll(properties.getPermitAllPathPatterns());
			permitAllPathPatterns = patterns.toArray(new String[patterns.size()]);
		} else {
			permitAllPathPatterns = new String[] { "/**" };
		}
		http.csrf().disable().authorizeRequests().antMatchers(permitAllPathPatterns).permitAll().anyRequest()
				.authenticated().and().exceptionHandling()
				.defaultAuthenticationEntryPointFor((request, response, ex) -> response
						.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getLocalizedMessage()),
						RequestUtils::isRequestedFromApi);
		setAuthenticationFilter(http.formLogin(),
				new RestfulUsernamePasswordAuthenticationFilter(authenticationManager(), objectMapper));
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

	private void setAuthenticationFilter(FormLoginConfigurer<HttpSecurity> formLogin,
			UsernamePasswordAuthenticationFilter authFilter) throws Exception {
		Field f = AbstractAuthenticationFilterConfigurer.class.getDeclaredField("authFilter");
		f.setAccessible(true); // AbstractAuthenticationFilterConfigurer::setAuthenticationFilter not visible
		f.set(formLogin, authFilter);
	}

	@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean(); // Expose AuthenticationManager as Bean
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