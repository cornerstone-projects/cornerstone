package com.example.demo.core.web;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.classmate.TypeResolver;

import io.swagger.annotations.ApiParam;
import lombok.Getter;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "springfox.documentation.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {

	@Bean
	public Docket docket() {
		return new Docket(DocumentationType.SWAGGER_2).ignoredParameterTypes(AuthenticationPrincipal.class).select()
				.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)).build();
	}

	@Bean
	public AlternateTypeRuleConvention customizeConvention(TypeResolver resolver) {
		return new AlternateTypeRuleConvention() {
			@Override
			public int getOrder() {
				return Ordered.LOWEST_PRECEDENCE;
			}

			@Override
			public List<AlternateTypeRule> rules() {
				return Arrays.asList(newRule(Pageable.class, SwaggerPageable.class));
			}
		};
	}

	@Getter
	private static class SwaggerPageable {

		@ApiParam(value = "Results page you want to retrieve (1..N)", example = "1")
		@Nullable
		private Integer page;

		@ApiParam(value = "Number of records per page", example = "10")
		@Nullable
		private Integer size;

		@ApiParam(value = "Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported.")
		@Nullable
		private String sort;

	}
}