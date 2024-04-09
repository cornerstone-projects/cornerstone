package io.cornerstone.core.web;

import java.util.Date;

import io.cornerstone.core.security.SecurityProperties;
import io.cornerstone.core.security.verification.VerificationManager;
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
import jakarta.annotation.PostConstruct;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;

import static io.cornerstone.core.security.DefaultWebAuthenticationDetails.PARAMETER_NAME_VERIFICATION_CODE;
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
	OpenApiCustomizer springSecurityLoginEndpointCustomizer(ObjectProvider<SecurityProperties> propertiesProvider,
			ObjectProvider<VerificationManager> verificationManagerProvider) {
		return openAPI -> propertiesProvider.ifAvailable(properties -> {
			Operation operation = new Operation();
			Schema<?> requestSchema = new ObjectSchema()
				.addProperty(properties.getUsernameParameter(), new StringSchema()._default("user"))
				.addProperty(properties.getPasswordParameter(), new StringSchema()._default("password"));
			verificationManagerProvider.ifAvailable(vm -> requestSchema.addProperty(PARAMETER_NAME_VERIFICATION_CODE,
					new StringSchema()._default("000000")));
			RequestBody requestBody = new RequestBody()
				.content(new Content().addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(requestSchema)));
			operation.requestBody(requestBody);
			ApiResponses apiResponses = new ApiResponses();
			Schema<?> okResponseSchema = new ObjectSchema()
				.addProperty("timestamp", new DateTimeSchema()._default(new Date()))
				.addProperty("status", new IntegerSchema()._default(OK.value()))
				.addProperty("message", new StringSchema()._default(OK.getReasonPhrase()))
				.addProperty("path", new StringSchema()._default(properties.getLoginProcessingUrl()))
				.addProperty("targetUrl", new StringSchema()._default("/"));
			apiResponses.addApiResponse(String.valueOf(OK.value()), new ApiResponse().description(OK.getReasonPhrase())
				.content(new Content().addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(okResponseSchema))));
			Schema<?> unauthorizedResponseSchema = new ObjectSchema()
				.addProperty("timestamp", new DateTimeSchema()._default(new Date()))
				.addProperty("status", new IntegerSchema()._default(UNAUTHORIZED.value()))
				.addProperty("error", new StringSchema()._default(UNAUTHORIZED.getReasonPhrase()))
				.addProperty("message", new StringSchema()._default("Bad Credentials"))
				.addProperty("path", new StringSchema()._default(properties.getLoginProcessingUrl()));
			apiResponses.addApiResponse(String.valueOf(UNAUTHORIZED.value()),
					new ApiResponse().description(UNAUTHORIZED.getReasonPhrase())
						.content(new Content().addMediaType(APPLICATION_JSON_VALUE,
								new MediaType().schema(unauthorizedResponseSchema))));
			operation.responses(apiResponses);
			operation.addTagsItem("login-endpoint");
			PathItem pathItem = new PathItem().post(operation);
			openAPI.getPaths().addPathItem(properties.getLoginProcessingUrl(), pathItem);
		});
	}

}
