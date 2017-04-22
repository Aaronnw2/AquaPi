package org.aqpi.api.model.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=BAD_REQUEST)
public class BadRequestException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		super(message);
	}
	
	public BadRequestException(String message, Throwable e) {
		super(message, e);
	}
}
