package io.cornerstone.core.web;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
public class RestTemplateConfiguration {

	@Value("${restTemplate.connectTimeout:5s}")
	private Duration connectTimeout;

	@Value("${restTemplate.readTimeout:30s}")
	private Duration readTimeout;

	@Bean
	RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer restTemplateBuilderConfigurer) {
		RestTemplateBuilder builder = new RestTemplateBuilder().setConnectTimeout(this.connectTimeout)
			.setReadTimeout(this.readTimeout)
			.requestFactory(JdkClientHttpRequestFactory.class);
		return restTemplateBuilderConfigurer.configure(builder);
	}

}
