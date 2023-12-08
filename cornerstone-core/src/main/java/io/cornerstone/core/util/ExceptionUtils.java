package io.cornerstone.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionUtils {

	private static final int MAX_DEPTH = 10;

	public static String getStackTraceAsString(Throwable t) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os, true, StandardCharsets.UTF_8);
			t.printStackTrace(ps);
			ps.flush();
			ps.close();
			String s = os.toString(StandardCharsets.UTF_8);
			os.flush();
			os.close();
			return s;
		}
		catch (IOException ex) {
			return t.getCause().toString();
		}
	}

	public static Throwable getRootCause(Throwable t) {
		int depth = MAX_DEPTH;
		while ((t.getCause() != null) && (depth > 0)) {
			depth--;
			t = t.getCause();
		}
		return t;
	}

	public static String getRootMessage(Throwable t) {
		return getRootCause(t).getMessage();
	}

	public static String getDetailMessage(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(t.getClass().getName()).append(":").append(t.getLocalizedMessage());
		int maxDepth = 10;
		while ((t.getCause() != null) && (maxDepth > 0)) {
			maxDepth--;
			t = t.getCause();
			sb.append("\n").append(t.getClass().getName());
			if ((t.getLocalizedMessage() != null) && (sb.indexOf(t.getLocalizedMessage()) < 0)) {
				sb.append(":").append(t.getLocalizedMessage());
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public static <E extends Throwable, R> R sneakyThrow(Throwable ex) throws E {
		throw (E) ex;
	}

}
