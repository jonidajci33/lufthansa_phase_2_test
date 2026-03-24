package com.planningpoker.estimation.domain;

import java.util.List;

/**
 * Generic domain page wrapper for paginated query results.
 */
public record Page<T>(
        List<T> content,
        long totalElements
) {
}
