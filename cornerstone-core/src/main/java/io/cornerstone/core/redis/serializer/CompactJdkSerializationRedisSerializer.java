package io.cornerstone.core.redis.serializer;

import java.io.EOFException;
import java.io.ObjectStreamConstants;
import java.io.StreamCorruptedException;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;

import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class CompactJdkSerializationRedisSerializer extends JdkSerializationRedisSerializer {

	public CompactJdkSerializationRedisSerializer() {
		super();
	}

	public CompactJdkSerializationRedisSerializer(@Nullable ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	public byte[] serialize(Object object) {
		if (object instanceof String str) {
			return str.getBytes(StandardCharsets.UTF_8);
		}
		return super.serialize(object);
	}

	@Override
	public Object deserialize(byte[] bytes) {
		try {
			return super.deserialize(bytes);
		}
		catch (SerializationException se) {
			if (!isJavaSerialized(bytes) && se.getCause() instanceof SerializationFailedException) {
				Throwable cause = se.getCause().getCause();
				if ((cause instanceof StreamCorruptedException || cause instanceof EOFException) && isUtf8(bytes)) {
					return new String(bytes, StandardCharsets.UTF_8);
				}
			}
			throw se;
		}
	}

	private static boolean isJavaSerialized(byte[] bytes) {
		if (bytes.length > 2) {
			short magic = (short) ((bytes[1] & 0xFF) + (bytes[0] << 8));
			return magic == ObjectStreamConstants.STREAM_MAGIC;
		}
		return false;
	}

	public static boolean isUtf8(byte[] bytes) {
		int expectedLength;
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] & 0b10000000) == 0b00000000) {
				expectedLength = 1;
			}
			else if ((bytes[i] & 0b11100000) == 0b11000000) {
				expectedLength = 2;
			}
			else if ((bytes[i] & 0b11110000) == 0b11100000) {
				expectedLength = 3;
			}
			else if ((bytes[i] & 0b11111000) == 0b11110000) {
				expectedLength = 4;
			}
			else if ((bytes[i] & 0b11111100) == 0b11111000) {
				expectedLength = 5;
			}
			else if ((bytes[i] & 0b11111110) == 0b11111100) {
				expectedLength = 6;
			}
			else {
				return false;
			}
			while (--expectedLength > 0) {
				if (++i >= bytes.length) {
					return false;
				}
				if ((bytes[i] & 0b11000000) != 0b10000000) {
					return false;
				}
			}
		}
		return true;
	}

}
