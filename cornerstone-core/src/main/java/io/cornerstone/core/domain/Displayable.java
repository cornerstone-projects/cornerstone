package io.cornerstone.core.domain;

import io.cornerstone.core.util.I18N;

public interface Displayable {

	String name();

	default String getName() {
		return name();
	}

	default String getDisplayName() {
		try {
			return I18N.getMessage(getClass(), getName());
		} catch (Exception e) {
			return getName();
		}
	}

}
