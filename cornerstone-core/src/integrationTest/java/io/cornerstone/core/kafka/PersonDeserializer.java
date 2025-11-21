package io.cornerstone.core.kafka;

import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

public class PersonDeserializer extends JacksonJsonDeserializer<Person> {

	public PersonDeserializer() {
		super(Person.class, false);
	}

}
