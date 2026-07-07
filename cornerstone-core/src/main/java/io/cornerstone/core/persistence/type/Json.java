package io.cornerstone.core.persistence.type;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.Type;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Type(JsonType.class)
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Json {

}
