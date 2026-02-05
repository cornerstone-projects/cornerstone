package io.cornerstone.core.web;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final MessageSource messageSource;

	@ExceptionHandler(OptimisticLockingFailureException.class)
	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "optimistic.locking.failure")
	public void handleConflict() {

	}

	@ExceptionHandler
	public void handleMaxUploadSizeExceededException(HttpServletRequest request, HttpServletResponse response,
			MaxUploadSizeExceededException ex) throws IOException {
		response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, this.messageSource.getMessage(
				"max.upload.size.exceeded", new Object[] { ex.getMaxUploadSize() }, LocaleContextHolder.getLocale()));
	}

	@ExceptionHandler
	public void handlePropertyReferenceException(HttpServletRequest request, HttpServletResponse response,
			PropertyReferenceException ex) throws IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
	}

}
