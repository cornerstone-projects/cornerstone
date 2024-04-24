package io.cornerstone.core.util;

import lombok.experimental.UtilityClass;

import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;

@UtilityClass
public class AopUtils {

	// copy from AopTestUtils

	@SuppressWarnings("unchecked")
	public static <T> T getTargetObject(Object candidate) {
		Assert.notNull(candidate, "Candidate must not be null");
		try {
			if (org.springframework.aop.support.AopUtils.isAopProxy(candidate)
					&& candidate instanceof Advised advised) {
				Object target = advised.getTargetSource().getTarget();
				if (target != null) {
					return (T) target;
				}
			}
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Failed to unwrap proxied object", ex);
		}
		return (T) candidate;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getUltimateTargetObject(Object candidate) {
		Assert.notNull(candidate, "Candidate must not be null");
		try {
			if (org.springframework.aop.support.AopUtils.isAopProxy(candidate)
					&& candidate instanceof Advised advised) {
				Object target = advised.getTargetSource().getTarget();
				if (target != null) {
					return getUltimateTargetObject(target);
				}
			}
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Failed to unwrap proxied object", ex);
		}
		return (T) candidate;
	}

}
