package com.planningpoker.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response envelope per Error Response Schema standard.
 * Used by all services for consistent error responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String details,
        List<FieldError> errors,
        String correlationId,
        String path
) {

    public static ApiError of(int status, String code, String message, String correlationId, String path) {
        return new ApiError(Instant.now(), status, code, message, null, null, correlationId, path);
    }

    public static ApiError withDetails(int status, String code, String message, String details,
                                       String correlationId, String path) {
        return new ApiError(Instant.now(), status, code, message, details, null, correlationId, path);
    }

    public static ApiError withFieldErrors(int status, String code, String message,
                                            List<FieldError> errors, String correlationId, String path) {
        return new ApiError(Instant.now(), status, code, message, null, errors, correlationId, path);
    }

    /**
     * Represents a single field-level validation error.
     */
    public record FieldError(
            String field,
            String message,
            Object rejectedValue,
            String code
    ) {}
}
