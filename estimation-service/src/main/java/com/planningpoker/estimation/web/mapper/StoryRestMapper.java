package com.planningpoker.estimation.web.mapper;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.web.dto.StoryResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from domain {@link Story} to {@link StoryResponse} DTO.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class StoryRestMapper {

    private StoryRestMapper() {
        // utility class
    }

    /**
     * Converts a domain {@link Story} to a {@link StoryResponse} DTO.
     *
     * @param story the domain story (may be null)
     * @return the response DTO, or {@code null} if the input is null
     */
    public static StoryResponse toResponse(Story story) {
        if (story == null) {
            return null;
        }
        return new StoryResponse(
                story.getId(),
                story.getRoomId(),
                story.getTitle(),
                story.getDescription(),
                story.getStatus(),
                story.getSortOrder(),
                story.getFinalScore(),
                story.isConsensusReached(),
                story.getVoteCount(),
                story.getCreatedAt()
        );
    }

    /**
     * Converts a list of domain {@link Story} objects to {@link StoryResponse} DTOs.
     *
     * @param stories the domain stories (may be null)
     * @return an unmodifiable list of response DTOs; empty list if input is null or empty
     */
    public static List<StoryResponse> toResponseList(List<Story> stories) {
        if (stories == null || stories.isEmpty()) {
            return Collections.emptyList();
        }
        return stories.stream()
                .map(StoryRestMapper::toResponse)
                .toList();
    }
}
