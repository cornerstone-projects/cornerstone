package io.cornerstone.core.persistence.audit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.ValueGenerationType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@ValueGenerationType(generatedBy = CurrentUserGenerator.class)
@Target({ FIELD, METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CreationUser {

}
