package io.cornerstone.core.web;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
public class RestClientConfiguration {

	@Value("${restClient.readTimeout:30s}")
	private Duration readTimeout;

	@Bean
	RestClientCustomizer restClientCustomizer() {
		ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
			.withReadTimeout(this.readTimeout);
		ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
		return builder -> builder.requestFactory(requestFactory);
	}

}
