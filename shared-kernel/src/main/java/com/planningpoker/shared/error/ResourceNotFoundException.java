package com.planningpoker.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource does not exist.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceType, Object id) {
        super(HttpStatus.NOT_FOUND, resourceType.toUpperCase() + "_NOT_FOUND",
                resourceType + " not found with id: " + id);
    }
}
