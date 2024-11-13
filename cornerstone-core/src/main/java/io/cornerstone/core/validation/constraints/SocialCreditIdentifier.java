package io.cornerstone.core.validation.constraints;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.cornerstone.core.validation.validators.SocialCreditIdentifierValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = { SocialCreditIdentifierValidator.class })
public @interface SocialCreditIdentifier {

	String message() default "{io.cornerstone.core.validation.constraints.SocialCreditIdentifier.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
