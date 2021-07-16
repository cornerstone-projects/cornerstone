package com.example.demo.core.util;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestUtils {

	public static boolean isRequestedFromApi(HttpServletRequest request) {
		String contentType = request.getContentType();
		if (contentType != null) {
			if (MediaType.parseMediaType(contentType).isCompatibleWith(APPLICATION_JSON)) {
				return true;
			}
		}
		List<MediaType> accepts = MediaType.parseMediaTypes(request.getHeader(ACCEPT));
		if (accepts.isEmpty())
			return false;
		MediaType accept = accepts.get(0);
		return !accept.equals(ALL) && accept.isCompatibleWith(APPLICATION_JSON);
	}

}
