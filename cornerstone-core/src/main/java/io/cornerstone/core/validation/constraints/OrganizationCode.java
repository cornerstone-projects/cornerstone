package io.cornerstone.core.validation.constraints;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.cornerstone.core.validation.validators.OrganizationCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD,
		java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.CONSTRUCTOR,
		java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { OrganizationCodeValidator.class })
public @interface OrganizationCode {

	String message() default "{io.cornerstone.core.validation.constraints.OrganizationCode.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
