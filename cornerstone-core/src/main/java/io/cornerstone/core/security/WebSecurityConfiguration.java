package io.cornerstone.core.security;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cornerstone.core.Application;
import io.cornerstone.core.util.RequestUtils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.RequestCache;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private SecurityProperties properties;

	@Autowired(required = false)
	private List<IgnoredRequestContributor> ignoredRequestContributors = Collections.emptyList();

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void configure(WebSecurity web) {
		List<String> ignoringPathPatterns = new ArrayList<>();
		ignoringPathPatterns.addAll(Arrays.asList("/error", "/actuator/**", "/assets/**"));
		ignoringPathPatterns.addAll(this.properties.getIgnoringPathPatterns());
		this.ignoredRequestContributors.forEach(c -> ignoringPathPatterns.add(c.getIgnoringPathPattern()));
		web.ignoring().antMatchers(ignoringPathPatterns.toArray(new String[0]));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String[] permitAllPathPatterns;
		if (this.properties.isProtecting()) {
			List<String> patterns = new ArrayList<>();
			patterns.addAll(Arrays.asList(this.properties.getLoginPage(), this.properties.getLoginProcessingUrl(),
					this.properties.getLogoutUrl()));
			patterns.addAll(this.properties.getPermitAllPathPatterns());
			permitAllPathPatterns = patterns.toArray(new String[0]);
		}
		else {
			permitAllPathPatterns = new String[] { "/**" };
		}
		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = http
				.authorizeRequests();
		registry.antMatchers(permitAllPathPatterns).permitAll();
		this.properties.getAuthorizeRequestsMapping().forEach((k, v) -> {
			registry.antMatchers(k).hasAnyAuthority(v.split("\\s*,\\s*"));
		});
		registry.anyRequest().authenticated();

		http.exceptionHandling()
				.defaultAuthenticationEntryPointFor((request, response, ex) -> response
						.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getLocalizedMessage()),
						RequestUtils::isRequestedFromApi);

		setAuthenticationFilter(http.formLogin(),
				new RestfulUsernamePasswordAuthenticationFilter(authenticationManager(), this.objectMapper));
		http.formLogin().loginPage(this.properties.getLoginPage())
				.loginProcessingUrl(this.properties.getLoginProcessingUrl())
				.successHandler(authenticationSuccessHandler(http.getSharedObject(RequestCache.class)))
				.failureHandler(authenticationFailureHandler()).and().logout().logoutUrl(this.properties.getLogoutUrl())
				.logoutSuccessUrl(this.properties.getLoginPage());
		if (Application.current().map(Application::isUnitTest).orElse(true)) {
			http.httpBasic();
		}

		http.csrf().disable().headers().frameOptions().sameOrigin();
	}

	AuthenticationSuccessHandler authenticationSuccessHandler(RequestCache requestCache) {
		SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
		handler.setDefaultTargetUrl(this.properties.getDefaultSuccessUrl());
		if (requestCache != null) {
			handler.setRequestCache(requestCache);
		}
		handler.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
					throws IOException {
				if (RequestUtils.isRequestedFromApi(request)) {
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("timestamp", new Date());
					map.put("status", HttpStatus.OK.value());
					map.put("message", HttpStatus.OK.getReasonPhrase());
					map.put("path", WebSecurityConfiguration.this.properties.getLoginProcessingUrl());
					map.put("targetUrl", url);
					WebSecurityConfiguration.this.objectMapper.writeValue(response.getWriter(), map);
					// see DefaultErrorAttributes::getErrorAttributes
				}
				else {
					super.sendRedirect(request, response, url);
				}
			}
		});
		return handler;
	}

	AuthenticationFailureHandler authenticationFailureHandler() {
		return (request, response, ex) -> {
			if (RequestUtils.isRequestedFromApi(request)) {
				response.sendError(SC_UNAUTHORIZED, ex.getLocalizedMessage());
			}
			else {
				response.sendRedirect(this.properties.getLoginPage() + "?error");
				// request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
				// Method Not Allowed for LOGIN_PAGE
			}
		};
	}

	private void setAuthenticationFilter(FormLoginConfigurer<HttpSecurity> formLogin,
			UsernamePasswordAuthenticationFilter authFilter) throws Exception {
		Field f = AbstractAuthenticationFilterConfigurer.class.getDeclaredField("authFilter");
		f.setAccessible(true); // AbstractAuthenticationFilterConfigurer::setAuthenticationFilter
								// not visible
		f.set(formLogin, authFilter);
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider(ObjectProvider<UserDetailsService> userDetailsService,
			ObjectProvider<PasswordEncoder> passwordEncoder,
			ObjectProvider<UserDetailsPasswordService> userDetailsPasswordService,
			ObjectProvider<List<UserAuthorityMapper>> userAuthorityMappers) {
		UserDetailsService uds = userDetailsService.getIfAvailable(() -> new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				throw new UsernameNotFoundException(username);
			}
		});
		DefaultDaoAuthenticationProvider provider = new DefaultDaoAuthenticationProvider();
		provider.setUserDetailsService(uds);
		passwordEncoder.ifAvailable(provider::setPasswordEncoder);
		userDetailsPasswordService.ifAvailable(provider::setUserDetailsPasswordService);
		userAuthorityMappers.ifAvailable(provider::setUserAuthorityMappers);
		return provider;
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
