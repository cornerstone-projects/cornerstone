package io.cornerstone.core.util;

import java.util.concurrent.ThreadLocalRandom;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@UtilityClass
public class CodecUtils {

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String MDC_KEY_REQUEST_ID = "requestId";

	public static final String MDC_KEY_REQUEST = "request";

	public static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	public static String nextId() {
		return nextId(22); // back compatibility
	}

	public static String nextId(int length) {
		char[] chars = new char[length];
		chars[0] = CHARS[ThreadLocalRandom.current().nextInt(length == 22 ? 8 : CHARS.length)];
		for (int i = 1; i < chars.length; i++) {
			chars[i] = CHARS[ThreadLocalRandom.current().nextInt(CHARS.length)];
		}
		return new String(chars);
	}

	public static String generateRequestId() {
		String traceId = MDC.get("traceId");
		return traceId != null ? traceId : nextId();
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

	public static String randomDigitalString(int digits) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digits; i++) {
			sb.append(ThreadLocalRandom.current().nextInt(10));
		}
		return sb.toString();
	}

}
