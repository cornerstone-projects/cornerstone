package com.example.demo.core.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractRestController {

	@Autowired
	protected MessageSource messageSource;

	protected ResponseStatusException notFound(Object subject) {
		return new ResponseStatusException(NOT_FOUND,
				messageSource.getMessage("not.found", new Object[] { subject }, null));
	}

	protected ResponseStatusException invalidParam(String detail) {
		return new ResponseStatusException(BAD_REQUEST,
				messageSource.getMessage("invalid.param", new Object[] { detail }, null));
	}

}
