package io.cornerstone.core.validation.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.cornerstone.core.validation.validators.OrganizationCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
		ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { OrganizationCodeValidator.class })
public @interface OrganizationCode {

	String message() default "{io.cornerstone.core.validation.constraints.OrganizationCode.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
