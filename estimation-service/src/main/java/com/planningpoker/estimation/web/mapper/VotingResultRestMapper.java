package com.planningpoker.estimation.web.mapper;

import com.planningpoker.estimation.domain.VotingResult;
import com.planningpoker.estimation.web.dto.VotingResultResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from domain {@link VotingResult} to {@link VotingResultResponse} DTO.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class VotingResultRestMapper {

    private VotingResultRestMapper() {
        // utility class
    }

    /**
     * Converts a domain {@link VotingResult} to a {@link VotingResultResponse} DTO.
     *
     * @param result the domain voting result (may be null)
     * @return the response DTO, or {@code null} if the input is null
     */
    public static VotingResultResponse toResponse(VotingResult result) {
        if (result == null) {
            return null;
        }
        return new VotingResultResponse(
                result.storyId(),
                result.averageScore(),
                result.consensusReached(),
                result.totalVotes()
        );
    }

    /**
     * Converts a list of domain {@link VotingResult} objects to {@link VotingResultResponse} DTOs.
     *
     * @param results the domain voting results (may be null)
     * @return an unmodifiable list of response DTOs; empty list if input is null or empty
     */
    public static List<VotingResultResponse> toResponseList(List<VotingResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(VotingResultRestMapper::toResponse)
                .toList();
    }
}
