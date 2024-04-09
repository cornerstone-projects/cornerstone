package io.cornerstone.core.security.password;

import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.experimental.UtilityClass;

@UtilityClass
class MessageDigestUtils {

	public static final String DEFAULT_ENCODING = "UTF-8";

	private static final ThreadLocal<SoftReference<MessageDigest>> MD5 = ThreadLocal.withInitial(() -> {
		try {
			return new SoftReference<>(MessageDigest.getInstance("MD5"));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("md5 algorythm found");
		}
	});

	private static final ThreadLocal<SoftReference<MessageDigest>> SHA = ThreadLocal.withInitial(() -> {
		try {
			return new SoftReference<>(MessageDigest.getInstance("SHA"));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("sha algorythm found");
		}
	});

	private static final ThreadLocal<SoftReference<MessageDigest>> SHA256 = ThreadLocal.withInitial(() -> {
		try {
			return new SoftReference<>(MessageDigest.getInstance("SHA-256"));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("sha algorythm found");
		}
	});

	static byte[] md5(byte[] input) {
		SoftReference<MessageDigest> instanceRef = MD5.get();
		MessageDigest md5;
		if (instanceRef == null || (md5 = instanceRef.get()) == null) {
			try {
				md5 = MessageDigest.getInstance("MD5");
				instanceRef = new SoftReference<>(md5);
			}
			catch (NoSuchAlgorithmException ex) {
				throw new IllegalStateException("md5 algorythm found");
			}
			MD5.set(instanceRef);
		}
		md5.reset();
		md5.update(input);
		return md5.digest();
	}

	static byte[] sha(byte[] input) {
		SoftReference<MessageDigest> instanceRef = SHA.get();
		MessageDigest sha;
		if (instanceRef == null || (sha = instanceRef.get()) == null) {
			try {
				sha = MessageDigest.getInstance("SHA");
				instanceRef = new SoftReference<>(sha);
			}
			catch (NoSuchAlgorithmException ex) {
				throw new IllegalStateException("sha algorythm found");
			}
			SHA.set(instanceRef);
		}
		sha.reset();
		sha.update(input);
		return sha.digest();
	}

	static byte[] sha256(byte[] input) {
		SoftReference<MessageDigest> instanceRef = SHA256.get();
		MessageDigest sha256;
		if (instanceRef == null || (sha256 = instanceRef.get()) == null) {
			try {
				sha256 = MessageDigest.getInstance("SHA-256");
				instanceRef = new SoftReference<>(sha256);
			}
			catch (NoSuchAlgorithmException ex) {
				throw new IllegalStateException("sha-256 algorythm found");
			}
			SHA256.set(instanceRef);
		}
		sha256.reset();
		sha256.update(input);
		return sha256.digest();
	}

}
