package io.cornerstone.core.web;

import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@EnableConfigurationProperties(HttpClientProperties.class)
@Configuration(proxyBeanMethods = false)
public class RestTemplateConfiguration {

	@Bean
	RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer restTemplateBuilderConfigurer,
			HttpClientProperties properties) {
		RestTemplateBuilder builder = new RestTemplateBuilder().setConnectTimeout(properties.getConnectTimeout())
			.setReadTimeout(properties.getReadTimeout())
			.requestFactory(JdkClientHttpRequestFactory.class);
		return restTemplateBuilderConfigurer.configure(builder);
	}

}
