package io.cornerstone.core.security.verification;

@FunctionalInterface
public interface VerificationCodeGenerator {

	String generator(String receiver, int length);

}
