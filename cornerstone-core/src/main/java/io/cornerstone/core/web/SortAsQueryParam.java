package io.cornerstone.core.web;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Parameter(in = ParameterIn.QUERY,
		description = "Sorting criteria in the format: property(,asc|desc). " + "Default sort order is ascending. "
				+ "Multiple sort criteria are supported.",
		name = "sort", array = @ArraySchema(schema = @Schema(type = "string", example = "id")))
public @interface SortAsQueryParam {

}
