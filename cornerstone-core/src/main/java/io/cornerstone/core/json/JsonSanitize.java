package io.cornerstone.core.json;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * properties are ignored in JsonSanitizer.toJson()
 *
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface JsonSanitize {

	String DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";

	String value() default DEFAULT_NONE;

	int position() default -1;

}
