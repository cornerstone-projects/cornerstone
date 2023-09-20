package io.cornerstone.core.kafka;

import org.springframework.kafka.support.serializer.JsonDeserializer;

public class PersonDeserializer extends JsonDeserializer<Person> {

	public PersonDeserializer() {
		super(Person.class, false);
	}

}
