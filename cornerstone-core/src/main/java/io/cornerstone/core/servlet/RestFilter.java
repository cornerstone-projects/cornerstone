package io.cornerstone.core.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import static jakarta.servlet.DispatcherType.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.*;

public class RestFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger("rest");

	private static final String ATTR_NAME_RESPONSE_WRAPPED = LoggingBodyHttpServletResponse.class.getName()
			+ ".WRAPPED";

	@Value("${restFilter.loggingBody:true}")
	private boolean loggingBody = true;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		boolean isRequestDispatcher = request.getDispatcherType() == REQUEST;
		if (isRequestDispatcher) {
			boolean skip = !this.loggingBody;
			if (!skip) {
				List<MediaType> accepts = MediaType.parseMediaTypes(request.getHeader(ACCEPT));
				MediaType accept = accepts.isEmpty() ? APPLICATION_JSON : accepts.getFirst();
				skip = !accept.isCompatibleWith(APPLICATION_JSON) && !accept.isCompatibleWith(TEXT_PLAIN);
			}
			if (skip) {
				filterChain.doFilter(request, response);
				return;
			}
			if (request.getContentType() == null) {
				request = new WrappedHttpServletRequest(request);
			}
		}

		String contentType = request.getContentType();
		if ((contentType == null) || contentType.startsWith(TEXT_PLAIN_VALUE)
				|| contentType.startsWith(APPLICATION_JSON_VALUE)) {
			if (isRequestDispatcher) {
				if (request.getMethod().equals("GET") || request.getMethod().equals("DELETE")) {
					LOGGER.info("");
				}
				else {
					request = new LoggingBodyHttpServletRequest(request, LOGGER);
				}
			}
			if ((request.getAttribute(ATTR_NAME_RESPONSE_WRAPPED) == null) || (request.getDispatcherType() == ERROR)) {
				response = new LoggingBodyHttpServletResponse(response, LOGGER, request.getCharacterEncoding());
				request.setAttribute(ATTR_NAME_RESPONSE_WRAPPED, Boolean.TRUE);
			}
		}

		filterChain.doFilter(request, response);

		if (request.getAttribute(ATTR_NAME_RESPONSE_WRAPPED) != null) {
			contentType = response.getContentType();
			if (contentType != null) {
				MediaType type = MediaType.parseMediaType(contentType);
				if (type.isCompatibleWith(APPLICATION_JSON) || type.isCompatibleWith(TEXT_PLAIN)) {
					response.getOutputStream().close();
				}
			}
		}
	}

	private static class WrappedHttpServletRequest extends HttpServletRequestWrapper {

		WrappedHttpServletRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getContentType() {
			String contentType = super.getContentType();
			return contentType != null ? contentType : APPLICATION_JSON_VALUE;
		}

		@Override
		public String getHeader(String name) {
			if (CONTENT_TYPE.equalsIgnoreCase(name)) {
				return getContentType();
			}
			return super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			if (CONTENT_TYPE.equalsIgnoreCase(name)) {
				return Collections.enumeration(List.of(getContentType()));
			}
			return super.getHeaders(name);
		}

	}

}
