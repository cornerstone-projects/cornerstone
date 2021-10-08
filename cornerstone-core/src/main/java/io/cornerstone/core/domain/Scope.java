package io.cornerstone.core.domain;

public enum Scope implements Displayable {

	LOCAL, // only this jvm
	APPLICATION, // all jvm for this application
	GLOBAL; // all jvm for all application

	@Override
	public String toString() {
		return getDisplayName();
	}

}
