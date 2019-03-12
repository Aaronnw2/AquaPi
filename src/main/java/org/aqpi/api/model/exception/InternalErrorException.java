package org.aqpi.api.model.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=INTERNAL_SERVER_ERROR)
public class InternalErrorException extends Exception {
	private static final long serialVersionUID = 1L;

	public InternalErrorException(String message) {
		super(message);
	}
	
	public InternalErrorException(String message, Throwable e) {
		super(message, e);
	}
}
