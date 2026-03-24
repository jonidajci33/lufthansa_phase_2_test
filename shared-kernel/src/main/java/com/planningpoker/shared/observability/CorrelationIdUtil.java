package com.planningpoker.shared.observability;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility for accessing the current correlation ID from MDC.
 * Useful for embedding in Kafka event headers or outgoing REST calls.
 */
public final class CorrelationIdUtil {

    private CorrelationIdUtil() {}

    /**
     * Get the current correlation ID from MDC, or generate one if missing.
     */
    public static String current() {
        String id = MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID);
        return id != null ? id : UUID.randomUUID().toString();
    }
}
