package io.cornerstone.core.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.swagger.annotations.ApiParam;
import springfox.documentation.builders.AlternateTypeBuilder;
import springfox.documentation.builders.AlternateTypePropertyBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "springfox.documentation.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {

	private static final String PAGE_DESCRIPTION = "Results page you want to retrieve (1..N)";

	private static final String SIZE_DESCRIPTION = "Number of records per page";

	private static final String SORT_DESCRIPTION = "Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported.";

	@Bean
	public Docket docket() {
		return new Docket(DocumentationType.SWAGGER_2).ignoredParameterTypes(AuthenticationPrincipal.class).select()
				.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)).build();
	}

	@Bean
	public AlternateTypeRuleConvention customizeConvention(ObjectProvider<SpringDataWebProperties> propertiesProvider) {
		SpringDataWebProperties properties = propertiesProvider.getIfAvailable(SpringDataWebProperties::new);
		return new AlternateTypeRuleConvention() {
			@Override
			public int getOrder() {
				return Ordered.LOWEST_PRECEDENCE;
			}

			@Override
			public List<AlternateTypeRule> rules() {
				return Arrays.asList(newRule(Pageable.class, pageableMixin(properties)),
						newRule(Sort.class, sortMixin(properties)));
			}
		};
	}

	private static Class<?> pageableMixin(SpringDataWebProperties properties) {
		String pageParameter = properties.getPageable().getPageParameter();
		String pageDefault = String.valueOf(properties.getPageable().isOneIndexedParameters() ? 1 : 0);
		String sizeParameter = properties.getPageable().getSizeParameter();
		String sizeDefault = String.valueOf(properties.getPageable().getDefaultPageSize());
		String sortParameter = properties.getSort().getSortParameter();
		return new AlternateTypeBuilder()
				.fullyQualifiedClassName(String.format("%s.generated.%s", Pageable.class.getPackage().getName(),
						Pageable.class.getSimpleName()))
				.property(p -> p.name(pageParameter).type(Integer.class).canRead(true).canWrite(true)
						.annotations(Collections.singletonList(new ApiParamBuilder().value(PAGE_DESCRIPTION)
								.defaultValue(pageDefault).example(pageDefault).build())))
				.property(p -> p.name(sizeParameter).type(Integer.class).canRead(true).canWrite(true)
						.annotations(Collections.singletonList(new ApiParamBuilder().value(SIZE_DESCRIPTION)
								.defaultValue(sizeDefault).example(sizeDefault).build())))
				.property(sortParam(sortParameter)).build();
	}

	private static Class<?> sortMixin(SpringDataWebProperties properties) {
		return new AlternateTypeBuilder()
				.fullyQualifiedClassName(
						String.format("%s.generated.%s", Sort.class.getPackage().getName(), Sort.class.getSimpleName()))
				.property(sortParam(properties.getSort().getSortParameter())).build();
	}

	private static Consumer<AlternateTypePropertyBuilder> sortParam(String sortParameter) {
		return p -> p.name(sortParameter).type(String.class).canRead(true).canWrite(true).annotations(
				Collections.singletonList(new ApiParamBuilder().value(SORT_DESCRIPTION).allowMultiple(true).build()));
	}

	private static class ApiParamBuilder {

		private String value = "";

		private String defaultValue = "";

		private String example = "";

		private boolean allowMultiple = false;

		ApiParamBuilder value(String value) {
			this.value = value;
			return this;
		}

		ApiParamBuilder defaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		ApiParamBuilder example(String example) {
			this.example = example;
			return this;
		}

		ApiParamBuilder allowMultiple(boolean allowMultiple) {
			this.allowMultiple = allowMultiple;
			return this;
		}

		ApiParam build() {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("value", this.value);
			attributes.put("defaultValue", this.defaultValue);
			attributes.put("example", this.example);
			attributes.put("allowMultiple", this.allowMultiple);
			return AnnotationUtils.synthesizeAnnotation(attributes, ApiParam.class, null);
		}

	}

}
