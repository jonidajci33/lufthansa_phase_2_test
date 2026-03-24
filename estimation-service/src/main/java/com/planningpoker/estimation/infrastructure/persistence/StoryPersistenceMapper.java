package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.Vote;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link Story} domain objects and {@link StoryJpaEntity} persistence entities.
 * <p>
 * Delegates vote mapping to {@link VotePersistenceMapper}.
 * Pure utility class — no framework imports, null-safe.
 */
public final class StoryPersistenceMapper {

    private StoryPersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link StoryJpaEntity} to a domain {@link Story}.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain story, or {@code null} if the input is null
     */
    public static Story toDomain(StoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<Vote> domainVotes = VotePersistenceMapper.toDomainList(entity.getVotes());

        return new Story(
                entity.getId(),
                entity.getRoomId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getSortOrder(),
                entity.getFinalScore(),
                entity.isConsensusReached(),
                domainVotes,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Converts a domain {@link Story} to a {@link StoryJpaEntity}.
     *
     * @param story the domain story (may be null)
     * @return the JPA entity, or {@code null} if the input is null
     */
    public static StoryJpaEntity toEntity(Story story) {
        if (story == null) {
            return null;
        }
        StoryJpaEntity entity = new StoryJpaEntity();
        entity.setId(story.getId());
        entity.setRoomId(story.getRoomId());
        entity.setTitle(story.getTitle());
        entity.setDescription(story.getDescription());
        entity.setStatus(story.getStatus());
        entity.setSortOrder(story.getSortOrder());
        entity.setFinalScore(story.getFinalScore());
        entity.setConsensusReached(story.isConsensusReached());
        entity.setCreatedAt(story.getCreatedAt());
        entity.setUpdatedAt(story.getUpdatedAt());

        List<VoteJpaEntity> voteEntities = VotePersistenceMapper.toEntityList(story.getVotes(), entity);
        entity.setVotes(new java.util.ArrayList<>(voteEntities));

        return entity;
    }

    /**
     * Converts a list of {@link StoryJpaEntity} to domain {@link Story} objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain stories; empty list if input is null or empty
     */
    public static List<Story> toDomainList(List<StoryJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(StoryPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a list of domain {@link Story} objects to {@link StoryJpaEntity} instances.
     *
     * @param stories the domain stories (may be null)
     * @return an unmodifiable list of JPA entities; empty list if input is null or empty
     */
    public static List<StoryJpaEntity> toEntityList(List<Story> stories) {
        if (stories == null || stories.isEmpty()) {
            return Collections.emptyList();
        }
        return stories.stream()
                .map(StoryPersistenceMapper::toEntity)
                .toList();
    }
}
