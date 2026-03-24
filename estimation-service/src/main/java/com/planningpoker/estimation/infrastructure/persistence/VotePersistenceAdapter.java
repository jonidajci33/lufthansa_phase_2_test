package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.application.port.out.VotePersistencePort;
import com.planningpoker.estimation.domain.Vote;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements {@link VotePersistencePort}
 * by delegating to Spring Data JPA.
 */
@Component
public class VotePersistenceAdapter implements VotePersistencePort {

    private final VoteJpaRepository voteJpaRepository;
    private final StoryJpaRepository storyJpaRepository;

    public VotePersistenceAdapter(VoteJpaRepository voteJpaRepository,
                                  StoryJpaRepository storyJpaRepository) {
        this.voteJpaRepository = voteJpaRepository;
        this.storyJpaRepository = storyJpaRepository;
    }

    @Override
    public List<Vote> findByStoryId(UUID storyId) {
        return VotePersistenceMapper.toDomainList(
                voteJpaRepository.findByStoryId(storyId));
    }

    @Override
    public Optional<Vote> findByStoryIdAndUserId(UUID storyId, UUID userId) {
        return voteJpaRepository.findByStoryIdAndUserId(storyId, userId)
                .map(VotePersistenceMapper::toDomain);
    }

    @Override
    public Vote save(Vote vote) {
        StoryJpaEntity storyEntity = storyJpaRepository.findById(vote.getStoryId())
                .orElseThrow(() -> new IllegalArgumentException("Story not found: " + vote.getStoryId()));

        VoteJpaEntity entity = VotePersistenceMapper.toEntity(vote, storyEntity);
        VoteJpaEntity saved = voteJpaRepository.save(entity);
        return VotePersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByStoryId(UUID storyId) {
        voteJpaRepository.deleteByStoryId(storyId);
    }
}
