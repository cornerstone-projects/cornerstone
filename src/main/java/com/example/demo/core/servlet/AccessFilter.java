package com.example.demo.core.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import com.example.demo.core.Application;
import com.example.demo.core.util.CodecUtils;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessFilter implements Filter {

	public static final String HTTP_HEADER_INSTANCE_ID = "X-Instance-Id";
	public static final String HTTP_HEADER_REQUEST_ID = "X-Request-Id";
	public static final String MDC_KEY_REQUEST_ID = CodecUtils.MDC_KEY_REQUEST_ID;
	public static final String MDC_KEY_REQUEST = CodecUtils.MDC_KEY_REQUEST;

	private Logger accessLog = LoggerFactory.getLogger("access");

	private Logger accesWarnLog = LoggerFactory.getLogger("access-warn");

	public static final long DEFAULT_RESPONSETIMETHRESHOLD = 5000;

	public static final boolean DEFAULT_PRINT = true;

	@Getter
	@Setter
	@Value("${accessFilter.responseTimeThreshold:" + DEFAULT_RESPONSETIMETHRESHOLD + "}")
	public long responseTimeThreshold = DEFAULT_RESPONSETIMETHRESHOLD;

	@Setter
	@Value("${accessFilter.print:" + DEFAULT_PRINT + "}")
	private boolean print = DEFAULT_PRINT;

	@Autowired
	private Application application;

	private String serverTag;

	@Override
	public void init(FilterConfig filterConfig) {
		serverTag = " server:" + application.getInstanceId(true);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		LocaleContextHolder.setLocale(request.getLocale(), true);
		String uri = request.getRequestURI();
		uri = uri.substring(request.getContextPath().length());

		String remoteAddr = request.getRemoteAddr();
		MDC.put("remoteAddr", remoteAddr);
		MDC.put("method", request.getMethod());
		MDC.put("url", " " + request.getRequestURL());
		String s = request.getHeader("User-Agent");
		if (s == null)
			s = "";
		MDC.put("userAgent", " UserAgent:" + s);
		s = request.getHeader("Referer");
		if (s == null || !s.startsWith("http"))
			s = "";
		MDC.put("referer", " Referer:" + s);

		HttpSession session = request.getSession(false);
		String sessionId = null;
		if (session != null) {
			sessionId = session.getId();
			int index = sessionId.indexOf('-'); // uuid
			if (index > 0)
				sessionId = sessionId.replace("-", "");
		}
		String requestId = (String) request.getAttribute(HTTP_HEADER_REQUEST_ID);
		if (requestId == null) {
			requestId = request.getHeader(HTTP_HEADER_REQUEST_ID);
			if (!StringUtils.hasLength(requestId)) {
				requestId = CodecUtils.generateRequestId();
				response.setHeader(HTTP_HEADER_REQUEST_ID, requestId);
			}
			if (requestId.indexOf('.') < 0 && sessionId != null)
				requestId = new StringBuilder(sessionId).append('.').append(requestId).toString();
			request.setAttribute(HTTP_HEADER_REQUEST_ID, requestId);
		}
		MDC.put(MDC_KEY_REQUEST_ID, requestId);
		StringBuilder sb = new StringBuilder();
		sb.append(" request:");
		sb.append(requestId);
		MDC.put(MDC_KEY_REQUEST, sb.toString());

		MDC.put("server", serverTag);
		long start = System.nanoTime();
		try {
			chain.doFilter(request, response);
			long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
			if (responseTime > responseTimeThreshold) {
				StringBuilder msg = new StringBuilder();
				msg.append(request.getQueryString()).append(" response time:").append(responseTime).append("ms");
				accesWarnLog.warn(msg.toString());
				Metrics.timer("http.access.slow", Arrays.asList(Tag.of("uri", uri))).record(responseTime,
						TimeUnit.MILLISECONDS);
			}
		} catch (ServletException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			if (print && !uri.startsWith("/assets/") && request.getHeader("Last-Event-ID") == null) {
				long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
				MDC.put("responseTime", " responseTime:" + responseTime);
				accessLog.info("");
				Metrics.timer("http.access").record(responseTime, TimeUnit.MILLISECONDS);
			}
			MDC.clear();
		}

	}

	@Override
	public void destroy() {

	}

}
