package io.cornerstone.core.security.password;

import java.security.MessageDigest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.util.EncodingUtils;

public class MixedPasswordEncoder implements PasswordEncoder {

	private static final BytesKeyGenerator saltGenerator = KeyGenerators.secureRandom();

	private static final PasswordEncoder BCRYPT = new BCryptPasswordEncoder();

	@Override
	public String encode(CharSequence rawPassword) {
		if (rawPassword == null) {
			return null;
		}
		boolean additionalMD5 = false;
		byte[] digest = digest(rawPassword, saltGenerator.generateKey(), additionalMD5);
		return new String(Hex.encode(digest));
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (rawPassword == null || encodedPassword == null) {
			return false;
		}
		switch (encodedPassword.length()) {
		case 48:
			byte[] digested = Hex.decode(encodedPassword);
			byte[] salt = EncodingUtils.subArray(digested, 0, saltGenerator.getKeyLength());
			return MessageDigest.isEqual(digested, digest(rawPassword, salt, true));
		case 60:
			return BCRYPT.matches(rawPassword, encodedPassword);
		case 80:
			digested = Hex.decode(encodedPassword);
			salt = EncodingUtils.subArray(digested, 0, saltGenerator.getKeyLength());
			return MessageDigest.isEqual(digested, digest(rawPassword, salt, false));
		default:
			return false;
		}
	}

	private static byte[] digest(CharSequence rawPassword, byte[] salt, boolean additionalMD5) {
		// sha1 -> sha256 -> md5?
		if (rawPassword == null) {
			return null;
		}
		String password = rawPassword.toString();
		boolean isShaInput = password.length() == 40 && password.matches("\\p{XDigit}+");
		byte[] input = isShaInput ? Hex.decode(password) : Utf8.encode(password);
		if (input.length > 256) {
			return saltGenerator.generateKey(); // avoid long password DOS attack
		}
		if (!isShaInput) {
			input = MessageDigestUtils.sha(input);
		}
		byte[] digested = MessageDigestUtils.sha256(EncodingUtils.concatenate(new byte[][] { salt, input }));
		if (additionalMD5) {
			digested = MessageDigestUtils.md5(EncodingUtils.concatenate(new byte[][] { salt, digested }));
		}
		digested = EncodingUtils.concatenate(new byte[][] { salt, digested });
		return digested;
	}

}
