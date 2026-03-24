package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.Vote;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link Vote} domain objects and {@link VoteJpaEntity} persistence entities.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class VotePersistenceMapper {

    private VotePersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link VoteJpaEntity} to a domain {@link Vote}.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain vote, or {@code null} if the input is null
     */
    public static Vote toDomain(VoteJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Vote(
                entity.getId(),
                entity.getStory() != null ? entity.getStory().getId() : null,
                entity.getUserId(),
                entity.getValue(),
                entity.getNumericValue(),
                entity.isAnonymous(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Converts a domain {@link Vote} to a {@link VoteJpaEntity}.
     *
     * @param vote   the domain vote (may be null)
     * @param parent the owning story entity (required for the JPA relationship)
     * @return the JPA entity, or {@code null} if the vote is null
     */
    public static VoteJpaEntity toEntity(Vote vote, StoryJpaEntity parent) {
        if (vote == null) {
            return null;
        }
        VoteJpaEntity entity = new VoteJpaEntity();
        entity.setId(vote.getId());
        entity.setStory(parent);
        entity.setUserId(vote.getUserId());
        entity.setValue(vote.getValue());
        entity.setNumericValue(vote.getNumericValue());
        entity.setAnonymous(vote.isAnonymous());
        entity.setCreatedAt(vote.getCreatedAt());
        entity.setUpdatedAt(vote.getUpdatedAt());
        return entity;
    }

    /**
     * Converts a list of {@link VoteJpaEntity} to domain {@link Vote} objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain votes; empty list if input is null or empty
     */
    public static List<Vote> toDomainList(List<VoteJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(VotePersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a list of domain {@link Vote} objects to {@link VoteJpaEntity} instances.
     *
     * @param votes  the domain votes (may be null)
     * @param parent the owning story entity
     * @return an unmodifiable list of JPA entities; empty list if input is null or empty
     */
    public static List<VoteJpaEntity> toEntityList(List<Vote> votes, StoryJpaEntity parent) {
        if (votes == null || votes.isEmpty()) {
            return Collections.emptyList();
        }
        return votes.stream()
                .map(v -> toEntity(v, parent))
                .toList();
    }
}
