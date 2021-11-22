package io.cornerstone.core.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Parameter(in = ParameterIn.QUERY, description = "One-based page index (1..N)", name = "page",
		schema = @Schema(type = "integer", defaultValue = "1"))
@Parameter(in = ParameterIn.QUERY, description = "The size of the page to be returned", name = "size",
		schema = @Schema(type = "integer", defaultValue = "10"))
@Parameter(in = ParameterIn.QUERY,
		description = "Sorting criteria in the format: property(,asc|desc). " + "Default sort order is ascending. "
				+ "Multiple sort criteria are supported.",
		name = "sort", array = @ArraySchema(schema = @Schema(type = "string", example = "id")))
public @interface PageableAsQueryParam {

}
