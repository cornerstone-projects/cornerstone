package io.cornerstone.core.security.verification;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.cornerstone.core.security.PermitAllRequestContributor;
import io.cornerstone.core.security.VerificationCodeChecker;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(prefix = VerificationProperties.PREFIX, name = "enabled", havingValue = "true")
@RequestMapping(VerificationCodeController.PATH_PREFIX)
@RequiredArgsConstructor
public class VerificationCodeController implements PermitAllRequestContributor {

	public static final String PATH_PREFIX = "/verificationCode";

	private final VerificationManager verificationManager;

	private final List<VerificationCodeChecker> verificationCodeCheckers;

	private final VerificationProperties properties;

	@GetMapping("/{username}")
	public VerificationCodeRequirement requirement(@PathVariable String username) {
		if (this.verificationCodeCheckers.isEmpty() || !StringUtils.hasLength(username)
				|| this.verificationCodeCheckers.stream().allMatch(c -> c.skip(username))) {
			return new VerificationCodeRequirement(false, null, null, null, null);
		}
		boolean passwordHidden = this.verificationCodeCheckers.stream().allMatch(c -> c.skipPasswordCheck(username));
		boolean sendingRequired = this.verificationCodeCheckers.stream().anyMatch(c -> !c.skipSend());
		return new VerificationCodeRequirement(true, this.properties.getLength(), passwordHidden, sendingRequired,
				sendingRequired ? (int) this.properties.getResend().getInterval().toSeconds() : null);
	}

	@PostMapping("/{username}")
	public Map<String, Object> send(@PathVariable String username) {
		this.verificationManager.send(username);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("timestamp", new Date());
		result.put("status", 200);
		return result;
	}

	@Override
	public String getPermitAllPathPattern() {
		return PATH_PREFIX + "/*";
	}

}
