package io.cornerstone.core.web;

import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class DefaultWebMvcConfigurer implements WebMvcRegistrations, WebMvcConfigurer {

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return new DefaultRequestMappingHandlerMapping();
	}

}
