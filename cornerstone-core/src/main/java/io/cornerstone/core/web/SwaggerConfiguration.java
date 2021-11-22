package io.cornerstone.core.web;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.SpringDocUtils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {

	@PostConstruct
	private void init() {
		SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthenticationPrincipal.class, PageableDefault.class,
				SortDefault.class);
		StringSchema charsetSchema = new StringSchema();
		charsetSchema.setDefault(StandardCharsets.UTF_8);
		SpringDocUtils.getConfig().replaceWithSchema(Charset.class, charsetSchema);
	}

}
