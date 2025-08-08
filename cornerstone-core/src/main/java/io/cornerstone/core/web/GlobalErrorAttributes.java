package io.cornerstone.core.web;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.web.error.Error;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
		if (errorAttributes.containsKey("errors")) {
			Object errors = errorAttributes.get("errors");
			if (errors instanceof List) {
				String message = ((List<?>) errors).stream().map(element -> {
					if (element instanceof Error error) {
						return error.getDefaultMessage();
					}
					else if (element instanceof FieldError fieldError) {
						return fieldError.getDefaultMessage();
					}
					else {
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.joining("; "));
				errorAttributes.put("message", message);
			}
		}
		return errorAttributes;
	}

}
