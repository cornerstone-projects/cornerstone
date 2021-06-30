package com.example.demo.core.servlet;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
public class ServletConfiguration {

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

}
