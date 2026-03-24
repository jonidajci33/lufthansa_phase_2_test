package com.planningpoker.audit.domain;

import java.util.List;

/**
 * A simple page of results. Framework-agnostic replacement for Spring's Page.
 *
 * @param content       the items on this page
 * @param totalElements total number of matching items across all pages
 * @param <T>           element type
 */
public record Page<T>(List<T> content, long totalElements) {
}
