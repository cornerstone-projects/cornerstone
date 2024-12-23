package io.cornerstone.core.persistence.id;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(SequenceIdentifierGenerator.class)
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface SequenceIdentifier {

	String value();

}
