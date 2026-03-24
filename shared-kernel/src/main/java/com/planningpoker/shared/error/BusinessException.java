package com.planningpoker.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all business rule violations.
 * Services throw this; the GlobalExceptionHandler maps it to ApiError.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public BusinessException(String code, String message) {
        this(HttpStatus.BAD_REQUEST, code, message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
