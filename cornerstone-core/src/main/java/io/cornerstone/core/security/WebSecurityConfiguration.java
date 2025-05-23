package io.cornerstone.core.security;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cornerstone.core.Application;
import io.cornerstone.core.security.password.MixedPasswordEncoder;
import io.cornerstone.core.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class WebSecurityConfiguration {

	@Autowired
	private SecurityProperties properties;

	@Autowired(required = false)
	private List<IgnoringRequestContributor> ignoringRequestContributors = Collections.emptyList();

	@Autowired(required = false)
	private List<PermitAllRequestContributor> permitAllRequestContributors = Collections.emptyList();

	@Autowired
	private ObjectMapper objectMapper;

	public void configure(WebSecurity web) {
		List<String> ignoringPathPatterns = new ArrayList<>(this.properties.getIgnoringPathPatterns());
		this.ignoringRequestContributors.forEach(c -> ignoringPathPatterns.add(c.getIgnoringPathPattern()));
		web.ignoring()
			.requestMatchers(ignoringPathPatterns.stream()
				.map((s) -> PathPatternRequestMatcher.withDefaults().matcher(s))
				.toArray(PathPatternRequestMatcher[]::new));
	}

	protected void configure(HttpSecurity http) throws Exception {
		String[] permitAllPathPatterns;
		if (this.properties.isProtecting()) {
			List<String> patterns = new ArrayList<>();
			patterns.addAll(List.of("/error", this.properties.getLoginPage(), this.properties.getLoginProcessingUrl(),
					this.properties.getLogoutUrl()));
			patterns.addAll(this.properties.getPermitAllPathPatterns());
			this.permitAllRequestContributors.forEach(c -> patterns.add(c.getPermitAllPathPattern()));
			permitAllPathPatterns = patterns.toArray(new String[0]);
		}
		else {
			permitAllPathPatterns = new String[] { "/**" };
		}
		http.authorizeHttpRequests(configurer -> {
			configurer
				.requestMatchers(Stream.of(permitAllPathPatterns)
					.map((s) -> PathPatternRequestMatcher.withDefaults().matcher(s))
					.toArray(PathPatternRequestMatcher[]::new))
				.permitAll();
			this.properties.getAuthorizeRequestsMapping()
				.forEach((k, v) -> configurer.requestMatchers(k).hasAnyAuthority(v.split("\\s*,\\s*")));
			configurer.anyRequest().authenticated();
		});

		http.requestCache(RequestCacheConfigurer::disable);

		http.exceptionHandling(configurer -> configurer
			.defaultAuthenticationEntryPointFor((request, response,
					ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getLocalizedMessage()),
					RequestUtils::isRequestedFromApi)
			.defaultAuthenticationEntryPointFor((request, response, ex) -> {
				String targetUrl = this.properties.getLoginPage();
				if (!request.getRequestURI().equals(this.properties.getDefaultSuccessUrl())) {
					targetUrl += '?' + this.properties.getTargetUrlParameter() + '='
							+ URLEncoder.encode(request.getRequestURI(), StandardCharsets.UTF_8);
				}
				response.sendRedirect(targetUrl);
			}, request -> !RequestUtils.isRequestedFromApi(request)));

		http.formLogin(configurer -> {
			setAuthenticationFilter(configurer, new RestfulUsernamePasswordAuthenticationFilter(this.objectMapper));
			configurer.loginPage(this.properties.getLoginPage())
				.loginProcessingUrl(this.properties.getLoginProcessingUrl())
				.usernameParameter(this.properties.getUsernameParameter())
				.passwordParameter(this.properties.getPasswordParameter())
				.successHandler(authenticationSuccessHandler(http.getSharedObject(RequestCache.class)))
				.failureHandler(authenticationFailureHandler());
		})
			.logout(configurer -> configurer.logoutUrl(this.properties.getLogoutUrl())
				.logoutSuccessUrl(this.properties.getLoginPage()));
		if (Application.current().map(Application::isUnitTest).orElse(true)) {
			http.httpBasic(withDefaults());
		}
		http.csrf(AbstractHttpConfigurer::disable);
		http.headers(configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
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
				String targetUrl = request
					.getParameter(WebSecurityConfiguration.this.properties.getTargetUrlParameter());
				if (targetUrl != null && targetUrl.startsWith("/")) { // for security
					url = targetUrl;
				}
				if (RequestUtils.isRequestedFromApi(request)) {
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					WebSecurityConfiguration.this.objectMapper.writeValue(response.getWriter(), Collections
						.singletonMap(WebSecurityConfiguration.this.properties.getTargetUrlParameter(), url));
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
			UsernamePasswordAuthenticationFilter authFilter) {
		try {
			Field f = AbstractAuthenticationFilterConfigurer.class.getDeclaredField("authFilter");
			f.setAccessible(true); // AbstractAuthenticationFilterConfigurer::setAuthenticationFilter
									// not visible
			f.set(formLogin, authFilter);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Bean
	DefaultAuthenticationManager authenticationManager(List<AuthenticationProvider> providers) {
		return new DefaultAuthenticationManager(providers, this.properties);
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider(ObjectProvider<UserDetailsService> userDetailsService,
			ObjectProvider<PasswordEncoder> passwordEncoder,
			ObjectProvider<UserDetailsPasswordService> userDetailsPasswordService) {
		UserDetailsService uds = userDetailsService.getIfAvailable(() -> username -> {
			throw new UsernameNotFoundException(username);
		});
		DefaultDaoAuthenticationProvider provider = new DefaultDaoAuthenticationProvider(uds);
		passwordEncoder.ifAvailable(provider::setPasswordEncoder);
		userDetailsPasswordService.ifAvailable(provider::setUserDetailsPasswordService);
		return provider;
	}

	@Bean
	@ConditionalOnMissingBean
	PasswordEncoder passwordEncoder() {
		return new MixedPasswordEncoder();
	}

	@Bean
	GrantedAuthorityDefaults grantedAuthorityDefaults() {
		return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
			throws Exception {
		configure(http);
		return http.authenticationManager(authenticationManager).build();
	}

	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return this::configure;
	}

}
