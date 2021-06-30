package com.example.demo.core.util;

import java.util.Random;

import org.slf4j.MDC;

import com.example.demo.core.tracing.Tracing;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CodecUtils {

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String MDC_KEY_REQUEST_ID = "requestId";

	public static final String MDC_KEY_REQUEST = "request";

	public static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	private static final Random random = new Random();

	public static String nextId() {
		return nextId(22); // back compatibility
	}

	public static String nextId(int length) {
		char[] chars = new char[length];
		chars[0] = CHARS[random.nextInt(length == 22 ? 8 : CHARS.length)];
		for (int i = 1; i < chars.length; i++)
			chars[i] = CHARS[random.nextInt(CHARS.length)];
		return new String(chars);
	}

	public static String generateRequestId() {
		if (Tracing.isEnabled()) {
			Span span = GlobalTracer.get().activeSpan();
			if (span != null)
				return span.context().toTraceId();
		}
		return nextId();
	}

	public static boolean putRequestIdIfAbsent() {
		String requestId = MDC.get(MDC_KEY_REQUEST_ID);
		if (requestId == null) {
			requestId = generateRequestId();
			MDC.put(MDC_KEY_REQUEST_ID, requestId);
			MDC.put(MDC_KEY_REQUEST, " request:" + requestId);
			return true;
		}
		return false;
	}

	public static void removeRequestId() {
		MDC.remove(MDC_KEY_REQUEST_ID);
		MDC.remove(MDC_KEY_REQUEST);
	}

}
