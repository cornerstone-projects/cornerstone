package io.cornerstone.core.web;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
public class RestTemplateConfiguration {

	@Value("${restTemplate.connectTimeout:5s}")
	private Duration connectTimeout;

	@Value("${restTemplate.readTimeout:30s}")
	private Duration readTimeout;

	@Bean
	RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer restTemplateBuilderConfigurer) {
		RestTemplateBuilder builder = new RestTemplateBuilder().setConnectTimeout(this.connectTimeout)
			.setReadTimeout(this.readTimeout);
		return restTemplateBuilderConfigurer.configure(builder);
	}

	@Bean
	RestTemplateCustomizer restTemplateCustomizer() {
		return restTemplate -> {
			if (restTemplate.getRequestFactory() instanceof SimpleClientHttpRequestFactory requestFactory) {
				requestFactory.setOutputStreaming(false);
			}
		};
	}

}
