package io.cornerstone.core.util;

import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AopUtils {

	// copy from AopTestUtils

	@SuppressWarnings("unchecked")
	public static <T> T getTargetObject(Object candidate) {
		Assert.notNull(candidate, "Candidate must not be null");
		try {
			if (org.springframework.aop.support.AopUtils.isAopProxy(candidate) && candidate instanceof Advised) {
				Object target = ((Advised) candidate).getTargetSource().getTarget();
				if (target != null) {
					return (T) target;
				}
			}
		} catch (Throwable ex) {
			throw new IllegalStateException("Failed to unwrap proxied object", ex);
		}
		return (T) candidate;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getUltimateTargetObject(Object candidate) {
		Assert.notNull(candidate, "Candidate must not be null");
		try {
			if (org.springframework.aop.support.AopUtils.isAopProxy(candidate) && candidate instanceof Advised) {
				Object target = ((Advised) candidate).getTargetSource().getTarget();
				if (target != null) {
					return (T) getUltimateTargetObject(target);
				}
			}
		} catch (Throwable ex) {
			throw new IllegalStateException("Failed to unwrap proxied object", ex);
		}
		return (T) candidate;
	}

}
