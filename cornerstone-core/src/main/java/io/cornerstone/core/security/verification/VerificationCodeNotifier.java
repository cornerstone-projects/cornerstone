package io.cornerstone.core.security.verification;

@FunctionalInterface
public interface VerificationCodeNotifier {

	void send(String receiver, String code);

}
