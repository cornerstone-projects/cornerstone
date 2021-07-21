package com.example.demo.core.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractRestController {

	@Autowired
	protected ApplicationContext applicationContext;

	protected ResponseStatusException notFound(Object subject) {
		return new ResponseStatusException(NOT_FOUND,
				applicationContext.getMessage("not.found", new Object[] { String.valueOf(subject) }, LocaleContextHolder.getLocale()));
	}

	protected ResponseStatusException badRequest(String reason) {
		return new ResponseStatusException(BAD_REQUEST, reason);
	}

	protected ResponseStatusException invalidParam(String name) {
		return new ResponseStatusException(BAD_REQUEST,
				applicationContext.getMessage("invalid.param", new Object[] { name }, LocaleContextHolder.getLocale()));
	}

	protected ResponseStatusException missingParam(String name) {
		return new ResponseStatusException(BAD_REQUEST,
				applicationContext.getMessage("missing.param", new Object[] { name }, LocaleContextHolder.getLocale()));
	}

	protected RuntimeException shouldNeverHappen() {
		return new IllegalStateException("This should never happen!!!");
	}

}
