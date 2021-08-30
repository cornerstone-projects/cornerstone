package io.cornerstone.core.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final MessageSource messageSource;

	@ExceptionHandler(OptimisticLockingFailureException.class)
	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "optimistic.locking.failure")
	public void handleConflict() {

	}

	@ExceptionHandler
	public void handleMaxUploadSizeExceeded(HttpServletRequest request, HttpServletResponse response,
			MaxUploadSizeExceededException ex) throws IOException {
		response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, messageSource.getMessage(
				"max.upload.size.exceeded", new Object[] { ex.getMaxUploadSize() }, LocaleContextHolder.getLocale()));
	}

}