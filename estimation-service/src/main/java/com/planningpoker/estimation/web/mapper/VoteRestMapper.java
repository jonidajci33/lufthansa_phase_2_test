package com.planningpoker.estimation.web.mapper;

import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.web.dto.VoteResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from domain {@link Vote} to {@link VoteResponse} DTO.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class VoteRestMapper {

    private VoteRestMapper() {
        // utility class
    }

    /**
     * Converts a domain {@link Vote} to a {@link VoteResponse} DTO.
     *
     * @param vote the domain vote (may be null)
     * @return the response DTO, or {@code null} if the input is null
     */
    public static VoteResponse toResponse(Vote vote) {
        if (vote == null) {
            return null;
        }
        return new VoteResponse(
                vote.getId(),
                vote.getUserId(),
                vote.getValue(),
                vote.getNumericValue(),
                vote.getCreatedAt()
        );
    }

    /**
     * Converts a list of domain {@link Vote} objects to {@link VoteResponse} DTOs.
     *
     * @param votes the domain votes (may be null)
     * @return an unmodifiable list of response DTOs; empty list if input is null or empty
     */
    public static List<VoteResponse> toResponseList(List<Vote> votes) {
        if (votes == null || votes.isEmpty()) {
            return Collections.emptyList();
        }
        return votes.stream()
                .map(VoteRestMapper::toResponse)
                .toList();
    }
}
