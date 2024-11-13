package io.cornerstone.core.validation.constraints;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.cornerstone.core.validation.validators.OrganizationCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = { OrganizationCodeValidator.class })
public @interface OrganizationCode {

	String message() default "{io.cornerstone.core.validation.constraints.OrganizationCode.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
