package com.planningpoker.audit.domain;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageTest {

    @Test
    void shouldCreatePageWithContentAndTotal() {
        List<String> items = List.of("a", "b", "c");

        Page<String> page = new Page<>(items, 10L);

        assertThat(page.content()).containsExactly("a", "b", "c");
        assertThat(page.totalElements()).isEqualTo(10L);
    }

    @Test
    void shouldCreateEmptyPage() {
        Page<String> page = new Page<>(Collections.emptyList(), 0L);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }

    @Test
    void shouldPreserveGenericType() {
        Page<Integer> page = new Page<>(List.of(1, 2, 3), 100L);

        assertThat(page.content()).containsExactly(1, 2, 3);
        assertThat(page.totalElements()).isEqualTo(100L);
    }
}
