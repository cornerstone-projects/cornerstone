package io.cornerstone.core.util;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import org.springframework.http.MediaType;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.*;

@UtilityClass
public class RequestUtils {

	public static boolean isRequestedFromApi(HttpServletRequest request) {
		if (request.getHeader("X-Requested-With") != null) {
			return true;
		}
		String contentType = request.getContentType();
		if ((contentType != null) && MediaType.parseMediaType(contentType).isCompatibleWith(APPLICATION_JSON)) {
			return true;
		}
		List<MediaType> accepts = MediaType.parseMediaTypes(request.getHeader(ACCEPT));
		if (!accepts.isEmpty()) {
			MediaType accept = accepts.getFirst();
			return !accept.equals(ALL) && accept.isCompatibleWith(APPLICATION_JSON);
		}
		return false;
	}

}
