package com.example.demo.core.servlet;

import java.util.List;

import javax.servlet.DispatcherType;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.client.ClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
@Profile("!test")
public class ServletConfiguration {

	@Bean
	RestFilter restFilter() {
		return new RestFilter();
	}

	@Bean
	FilterRegistrationBean<RestFilter> restFilterRegistration(RestFilter restFilter) {
		FilterRegistrationBean<RestFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(restFilter);
		registrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC);
		registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 52);
		// after AccessFilter
		return registrationBean;
	}

	@Bean
	AccessFilter accessFilter() {
		return new AccessFilter();
	}

	@Bean
	FilterRegistrationBean<AccessFilter> accessFilterRegistration(AccessFilter accessFilter) {
		FilterRegistrationBean<AccessFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(accessFilter);
		registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 51);
		// after SessionRepositoryFilter.DEFAULT_ORDER
		return registrationBean;
	}

	@Bean
	@ConditionalOnProperty(prefix = "portal", name = "baseUrl")
	SsoFilter ssoFilter(RestTemplateBuilder builder, UserDetailsService userDetailsService) {
		return new SsoFilter(builder, userDetailsService);
	}

	@Bean
	@ConditionalOnBean(value = SsoFilter.class)
	FilterRegistrationBean<SsoFilter> ssoFilterRegistration(SsoFilter ssoFilter) {
		FilterRegistrationBean<SsoFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(ssoFilter);
		registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return registrationBean;
	}

	@Bean
	RestTemplateCustomizer myRestTemplateCustomizer() {
		return restTemplate -> {
			List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
			interceptors.add((request, body, execution) -> {
				String requestId = MDC.get(AccessFilter.MDC_KEY_REQUEST_ID);
				if (requestId != null)
					request.getHeaders().set(AccessFilter.HTTP_HEADER_REQUEST_ID, requestId);
				return execution.execute(request, body);
			});
			interceptors.add(new LoggingClientHttpRequestInterceptor());
		};
	}

}
