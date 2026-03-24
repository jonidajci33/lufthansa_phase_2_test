package com.planningpoker.estimation.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Request body for submitting a vote on a story.
 */
public record SubmitVoteRequest(
        @NotBlank String value,
        BigDecimal numericValue
) {}
