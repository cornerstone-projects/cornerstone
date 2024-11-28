package io.cornerstone.core.logging;

import io.cornerstone.core.Application;

import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;
import org.springframework.core.env.Environment;

class DefaultStructuredLoggingJsonMembersCustomizer implements StructuredLoggingJsonMembersCustomizer<String> {

	private final String serviceId;

	DefaultStructuredLoggingJsonMembersCustomizer(Environment env) {
		this.serviceId = Application.getInstanceId(env);
	}

	@Override
	public void customize(JsonWriter.Members<String> members) {
		members.add("service.id", this.serviceId);
	}

}
