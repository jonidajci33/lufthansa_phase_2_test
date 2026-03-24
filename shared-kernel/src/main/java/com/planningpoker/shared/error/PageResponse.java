package com.planningpoker.shared.error;

import java.util.List;

/**
 * Standard paginated response envelope per REST Conventions standard.
 * Wraps list endpoints with metadata.
 */
public record PageResponse<T>(
        List<T> data,
        Meta meta
) {

    public record Meta(
            long total,
            int limit,
            int offset,
            boolean hasNext
    ) {}

    public static <T> PageResponse<T> of(List<T> data, long total, int limit, int offset) {
        boolean hasNext = (offset + limit) < total;
        return new PageResponse<>(data, new Meta(total, limit, offset, hasNext));
    }
}
