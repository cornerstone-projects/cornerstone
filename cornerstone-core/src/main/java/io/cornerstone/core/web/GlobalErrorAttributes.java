package io.cornerstone.core.web;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.web.error.Error;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
		if (errorAttributes.containsKey("errors")) {
			Object errors = errorAttributes.get("errors");
			if (errors instanceof List) {
				@SuppressWarnings("unchecked")
				String message = ((List<Error>) errors).stream()
					.map(Error::getDefaultMessage)
					.collect(Collectors.joining("; "));
				errorAttributes.put("message", message);
			}
		}
		return errorAttributes;
	}

}
