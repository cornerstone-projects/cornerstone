package io.cornerstone.core.web;

import java.util.Date;

import javax.annotation.PostConstruct;

import io.cornerstone.core.security.SecurityProperties;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {

	@PostConstruct
	private void init() {
		SpringDocUtils.getConfig().addAnnotationsToIgnore(PageableDefault.class, SortDefault.class);
	}

	@Bean
	OpenApiCustomiser springSecurityLoginEndpointCustomiser(ObjectProvider<SecurityProperties> propertiesProvider) {
		return openAPI -> {
			propertiesProvider.ifAvailable(properties -> {
				Operation operation = new Operation();
				Schema<?> requestSchema = new ObjectSchema()
						.addProperties(properties.getUsernameParameter(), new StringSchema())
						.addProperties(properties.getPasswordParameter(), new StringSchema());
				RequestBody requestBody = new RequestBody().content(
						new Content().addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(requestSchema)));
				operation.requestBody(requestBody);
				ApiResponses apiResponses = new ApiResponses();
				Schema<?> okResponseSchema = new ObjectSchema()
						.addProperties("timestamp", new DateTimeSchema()._default(new Date()))
						.addProperties("status", new IntegerSchema()._default(OK.value()))
						.addProperties("message", new StringSchema()._default(OK.getReasonPhrase()))
						.addProperties("path", new StringSchema()._default(properties.getLoginProcessingUrl()))
						.addProperties("targetUrl", new StringSchema()._default("/"));
				apiResponses.addApiResponse(String.valueOf(OK.value()),
						new ApiResponse().description(OK.getReasonPhrase()).content(new Content()
								.addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(okResponseSchema))));
				Schema<?> unauthorizedResponseSchema = new ObjectSchema()
						.addProperties("timestamp", new DateTimeSchema()._default(new Date()))
						.addProperties("status", new IntegerSchema()._default(UNAUTHORIZED.value()))
						.addProperties("error", new StringSchema()._default(UNAUTHORIZED.getReasonPhrase()))
						.addProperties("message", new StringSchema()._default("Bad Credentials"))
						.addProperties("path", new StringSchema()._default(properties.getLoginProcessingUrl()));
				apiResponses.addApiResponse(String.valueOf(UNAUTHORIZED.value()),
						new ApiResponse().description(UNAUTHORIZED.getReasonPhrase())
								.content(new Content().addMediaType(APPLICATION_JSON_VALUE,
										new MediaType().schema(unauthorizedResponseSchema))));
				operation.responses(apiResponses);
				operation.addTagsItem("login-endpoint");
				PathItem pathItem = new PathItem().post(operation);
				openAPI.getPaths().addPathItem(properties.getLoginProcessingUrl(), pathItem);
			});
		};
	}

}
