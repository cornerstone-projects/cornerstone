package io.cornerstone.core.persistence.id;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@IdGeneratorType(SnowflakeIdentifierGenerator.class)
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface SnowflakeIdentifier {

}
